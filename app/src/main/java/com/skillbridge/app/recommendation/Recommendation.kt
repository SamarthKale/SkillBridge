package com.skillbridge.app.recommendation

import com.skillbridge.app.data.AppStatus
import com.skillbridge.app.data.Application
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.data.Profile

/** An opportunity ranked for a student. */
data class StudentRecommendation(
    val opportunity: Opportunity,
    val matchScorePercent: Int,
    val reasons: List<String>,
    val missingSkills: List<String>,
    /** Set when the student's profile is too thin for a reliable score. */
    val limitedProfileNote: String? = null
)

/** A student profile ranked for a client's project. */
data class CandidateRecommendation(
    val candidate: Profile,
    val matchScorePercent: Int,
    val reasons: List<String>,
    val missingSkills: List<String>,
    /** The candidate's application id for this project, if they applied. */
    val applicationId: String? = null
)

/**
 * Transparent, rule-based matching (no network, no model): skill overlap dominates,
 * with smaller boosts for category fit, keyword overlap and remote/location fit.
 */
object RecommendationEngine {

    private fun norm(s: String) = s.trim().lowercase()

    private fun tokens(vararg texts: String): Set<String> =
        texts.joinToString(" ").lowercase()
            .split(Regex("[^a-z0-9+#.]+"))
            .filter { it.length > 2 }
            .toSet()

    /** Skills match when equal or one contains the other ("React" ~ "React Native"). */
    private fun skillMatches(a: String, b: String): Boolean {
        val x = norm(a)
        val y = norm(b)
        return x.isNotEmpty() && y.isNotEmpty() && (x == y || x.contains(y) || y.contains(x))
    }

    private class Score(val percent: Int, val reasons: List<String>, val missing: List<String>)

    private fun score(
        wantedSkills: List<String>,
        haveSkills: List<String>,
        category: String,
        wantedTokens: Set<String>,
        haveTokens: Set<String>,
        locationFit: String?
    ): Score {
        val reasons = mutableListOf<String>()
        val matched = wantedSkills.filter { w -> haveSkills.any { skillMatches(w, it) } }
        val missing = wantedSkills - matched.toSet()

        val skillPart = if (wantedSkills.isEmpty()) 0.5 else matched.size.toDouble() / wantedSkills.size
        val overlap = wantedTokens.intersect(haveTokens).size
        val keywordPart = (overlap / 6.0).coerceAtMost(1.0)
        val categoryHit = category.isNotBlank() && (
            haveSkills.any { norm(it).contains(norm(category)) || norm(category).contains(norm(it)) } ||
                norm(category) in haveTokens)
        val categoryPart = if (categoryHit) 1.0 else 0.0
        val locationPart = if (locationFit != null) 1.0 else 0.0

        val total = 0.6 * skillPart + 0.2 * keywordPart + 0.1 * categoryPart + 0.1 * locationPart

        if (matched.isNotEmpty()) {
            reasons += "Matches ${matched.size} of ${wantedSkills.size} skills: ${matched.take(3).joinToString(", ")}"
        }
        if (categoryHit) reasons += "Relevant to the $category category"
        if (locationFit != null) reasons += locationFit
        if (overlap >= 3) reasons += "Profile keywords overlap with the description"
        if (reasons.isEmpty()) reasons += "Limited overlap with the stated requirements"

        return Score((total * 100).toInt().coerceIn(0, 100), reasons, missing)
    }

    fun forStudent(
        profile: Profile?,
        opportunities: List<Opportunity>,
        applications: List<Application>,
        excludeApplied: Boolean
    ): List<StudentRecommendation> {
        val appliedIds = applications.map { it.projectId }.toSet()
        val have = profile?.skills.orEmpty()
        val haveTokens = tokens(
            profile?.bio.orEmpty(), profile?.experience.orEmpty(),
            profile?.branch.orEmpty(), profile?.degree.orEmpty()
        )
        val limited = if (have.isEmpty()) "Add skills to your profile for more accurate matches." else null
        val myLocation = profile?.location.orEmpty()

        return opportunities
            .filter { !excludeApplied || it.id !in appliedIds }
            .map { o ->
                val fit = when {
                    o.remote || o.workArrangement == "Remote" -> "Remote-friendly"
                    myLocation.isNotBlank() && norm(o.location).contains(norm(myLocation)) -> "Based in your area"
                    else -> null
                }
                val s = score(
                    o.skills, have, o.category,
                    tokens(o.title, o.description, o.requirements), haveTokens, fit
                )
                StudentRecommendation(o, s.percent, s.reasons, s.missing, limited)
            }
            .sortedWith(
                compareByDescending<StudentRecommendation> { it.matchScorePercent }
                    .thenByDescending { it.opportunity.postedAt }
            )
    }

    fun forClient(
        project: Opportunity,
        studentProfiles: List<Profile>,
        proposals: List<Application>
    ): List<CandidateRecommendation> {
        val appByStudent = proposals.associateBy { it.studentUid }
        val wantedTokens = tokens(project.title, project.description, project.requirements, project.responsibilities)
        return studentProfiles
            .filter { it.role == "student" }
            .map { p ->
                val fit = when {
                    project.remote || project.workArrangement == "Remote" -> "Can work remotely"
                    p.location.isNotBlank() && norm(project.location).contains(norm(p.location)) -> "Located near the project"
                    else -> null
                }
                val s = score(
                    project.skills, p.skills, project.category, wantedTokens,
                    tokens(p.bio, p.experience, p.branch, p.degree), fit
                )
                val app = appByStudent[p.uid]
                val reasons = if (app != null && app.status != AppStatus.REJECTED) {
                    s.reasons + "Already applied (${app.status})"
                } else s.reasons
                CandidateRecommendation(p, s.percent, reasons, s.missing, app?.id)
            }
            .sortedByDescending { it.matchScorePercent }
    }
}

/** Memoises results per input set; screens call invalidateAll() after status changes. */
object RecommendationCache {
    private val student = HashMap<Int, List<StudentRecommendation>>()
    private val candidate = HashMap<Int, List<CandidateRecommendation>>()

    @Synchronized
    fun getOrComputeStudentRecommendations(
        profile: Profile?,
        opportunities: List<Opportunity>,
        applications: List<Application>,
        excludeApplied: Boolean
    ): List<StudentRecommendation> {
        val key = listOf(profile, opportunities, applications.map { it.projectId }, excludeApplied).hashCode()
        return student.getOrPut(key) {
            RecommendationEngine.forStudent(profile, opportunities, applications, excludeApplied)
        }
    }

    @Synchronized
    fun getOrComputeCandidateRecommendations(
        project: Opportunity,
        studentProfiles: List<Profile>,
        proposals: List<Application>,
        authenticatedClientUid: String?
    ): List<CandidateRecommendation> {
        val key = listOf(project, studentProfiles, proposals, authenticatedClientUid).hashCode()
        return candidate.getOrPut(key) {
            RecommendationEngine.forClient(project, studentProfiles, proposals)
        }
    }

    @Synchronized
    fun invalidateAll() {
        student.clear()
        candidate.clear()
    }
}
