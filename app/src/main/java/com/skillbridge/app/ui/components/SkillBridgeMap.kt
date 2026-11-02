package com.skillbridge.app.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson

/** Longitude first, latitude second (same order the old map SDK used). */
data class LngLat(val lng: Double, val lat: Double)

data class MapMarkerData(
    val id: String,
    val position: LngLat,
    val title: String,
    val snippet: String
)

private class MarkerJson(val id: String, val lat: Double, val lng: Double, val title: String, val snippet: String)

private val gson = Gson()

/**
 * OpenStreetMap view (Leaflet inside a WebView, tiles from tile.openstreetmap.org).
 * No API key needed. Leaflet is bundled in `assets/map/`, so only the tiles need internet.
 *
 * - [markers]: red job/student pins; tapping one fires [onMarkerClick] with its id.
 * - [userLocation]: blue "you are here" dot.
 * - [radiusKm]: optional circle drawn around [initialCenter] (used by "nearby" search).
 * The camera moves to [initialCenter]/[initialZoom] whenever either changes.
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun SkillBridgeMap(
    modifier: Modifier = Modifier,
    initialCenter: LngLat = LngLat(0.0, 0.0),
    initialZoom: Double = 1.0,
    markers: List<MapMarkerData> = emptyList(),
    userLocation: LngLat? = null,
    radiusKm: Double? = null,
    onMarkerClick: ((String) -> Unit)? = null
) {
    var ready by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val clickHandler by rememberUpdatedState(onMarkerClick)

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val main = Handler(Looper.getMainLooper())
            val leafletJs = ctx.assets.open("map/leaflet.js").bufferedReader().use { it.readText() }
            val leafletCss = ctx.assets.open("map/leaflet.css").bufferedReader().use { it.readText() }
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                // Keep map pans/pinch from being stolen by a parent scroll container.
                setOnTouchListener { v, e ->
                    when (e.actionMasked) {
                        MotionEvent.ACTION_DOWN -> v.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                addJavascriptInterface(object {
                    @JavascriptInterface fun onReady() { main.post { ready = true } }
                    @JavascriptInterface fun onMarker(id: String) { main.post { clickHandler?.invoke(id) } }
                }, "Android")
                loadDataWithBaseURL(
                    "https://skillbridge.local/",
                    mapHtml(leafletCss, leafletJs),
                    "text/html", "utf-8", null
                )
                webView = this
            }
        },
        onRelease = { it.destroy(); webView = null; ready = false }
    )

    val wv = webView
    LaunchedEffect(wv, ready, initialCenter, initialZoom) {
        if (wv != null && ready) {
            wv.evaluateJavascript("sb.setView(${initialCenter.lat},${initialCenter.lng},${initialZoom.toInt()})", null)
        }
    }
    LaunchedEffect(wv, ready, markers) {
        if (wv != null && ready) {
            val json = gson.toJson(markers.map {
                MarkerJson(it.id, it.position.lat, it.position.lng, it.title, it.snippet)
            })
            wv.evaluateJavascript("sb.setMarkers($json)", null)
        }
    }
    LaunchedEffect(wv, ready, userLocation) {
        if (wv != null && ready) {
            val js = userLocation?.let { "sb.setUser(${it.lat},${it.lng})" } ?: "sb.setUser(null,null)"
            wv.evaluateJavascript(js, null)
        }
    }
    LaunchedEffect(wv, ready, radiusKm, initialCenter) {
        if (wv != null && ready) {
            val js = radiusKm?.let { "sb.setRadius(${initialCenter.lat},${initialCenter.lng},${it * 1000})" }
                ?: "sb.setRadius(null,null,null)"
            wv.evaluateJavascript(js, null)
        }
    }
}

private fun mapHtml(css: String, js: String): String = """
<!DOCTYPE html><html><head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<style>$css
html,body{height:100%;margin:0;padding:0;background:#f4f4f4}
#map{position:absolute;top:0;left:0;right:0;bottom:0}
.pin{width:22px;height:22px;background:#d93025;border:2px solid #fff;border-radius:50% 50% 50% 0;
transform:rotate(-45deg);box-shadow:0 1px 4px rgba(0,0,0,.45)}
.pin::after{content:"";position:absolute;width:8px;height:8px;background:#fff;border-radius:50%;top:5px;left:5px}
.me{width:16px;height:16px;background:#2563EB;border:3px solid #fff;border-radius:50%;
box-shadow:0 0 0 6px rgba(37,99,235,.25)}
.pop b{display:block;margin-bottom:2px}
</style></head><body><div id="map"></div>
<script>$js</script>
<script>
var map=L.map('map',{zoomControl:true,attributionControl:true}).setView([20.5937,78.9629],5);
L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,
 attribution:'&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'}).addTo(map);
// WebView inside Compose can lay out #map with 0 CSS height; size it from the viewport explicitly.
function fit(){var m=document.getElementById('map');m.style.height=window.innerHeight+'px';m.style.width=window.innerWidth+'px';map.invalidateSize();}
window.addEventListener('resize',fit);fit();
var layer=L.layerGroup().addTo(map),me=null,circle=null;
var pinIcon=L.divIcon({className:'',html:'<div class="pin"></div>',iconSize:[22,22],iconAnchor:[11,22],popupAnchor:[0,-22]});
var meIcon=L.divIcon({className:'',html:'<div class="me"></div>',iconSize:[16,16],iconAnchor:[8,8]});
window.sb={
 setView:function(lat,lng,z){map.invalidateSize();map.setView([lat,lng],z);},
 setMarkers:function(list){
  layer.clearLayers();
  list.forEach(function(m){
   var mk=L.marker([m.lat,m.lng],{icon:pinIcon}).addTo(layer);
   var d=document.createElement('div');d.className='pop';
   var b=document.createElement('b');b.textContent=m.title;d.appendChild(b);
   if(m.snippet){var s=document.createElement('span');s.textContent=m.snippet;d.appendChild(s);}
   mk.bindPopup(d);
   mk.on('click',function(){Android.onMarker(m.id);});
  });
 },
 setUser:function(lat,lng){
  if(me){map.removeLayer(me);me=null;}
  if(lat!==null){me=L.marker([lat,lng],{icon:meIcon,interactive:false,zIndexOffset:1000}).addTo(map);}
 },
 setRadius:function(lat,lng,m){
  if(circle){map.removeLayer(circle);circle=null;}
  if(lat!==null){circle=L.circle([lat,lng],{radius:m,color:'#2563EB',weight:2,fillColor:'#2563EB',fillOpacity:0.08}).addTo(map);
   map.invalidateSize();map.fitBounds(circle.getBounds(),{padding:[16,16]});}
 }
};
Android.onReady();
</script></body></html>
"""
