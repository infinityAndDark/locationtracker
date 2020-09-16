package com.demo.app.speedlocation.screen.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.postDelayed
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.app.speedlocation.MyAdapter
import com.demo.app.speedlocation.R
import com.demo.app.speedlocation.data.*
import com.demo.app.speedlocation.databinding.ItemHistoryBinding
import com.demo.app.speedlocation.helper.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*

class HistoryAdapter : MyAdapter() {
    private lateinit var iconMarkerHead: BitmapDescriptor
    private lateinit var iconMarkerStart: BitmapDescriptor
    private lateinit var iconMarkerEnd: BitmapDescriptor
    private var data: MutableList<HistoryModel> = ArrayList()
    private var fragmentManager: FragmentManager? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return createItem(parent)
    }

    fun setFragmentManager(manager: FragmentManager?) {
        this.fragmentManager = manager
    }

    fun setData(context: Context, list: List<HistoryModel>?, onDone: () -> Unit) {
        clearData()
        backgroundLaunch {
            if (!list.isNullOrEmpty()) {
                inMain {
                    iconMarkerHead = context.makeMarkerIcon(R.layout.view_marker_head)
                    iconMarkerStart = context.makeMarkerIcon(R.layout.view_marker_start)
                    iconMarkerEnd = context.makeMarkerIcon(R.layout.view_marker_end)
                }
                data.addAll(list)
                inMain { notifyDataSetChanged() }
            }
            inMain { onDone() }
        }
    }

    private fun clearData() {
        if (data.isNotEmpty()) {
            data.clear()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position >= data.size) return
        val item = data[position]
        when (holder) {
            is ItemViewHolder -> holder.bind(item)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is ItemViewHolder -> {
                holder.clear()
            }
        }
    }

    private fun createItem(parent: ViewGroup): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            ItemHistoryBinding.inflate(layoutInflater, parent, false)
        return ItemViewHolder(binding)
    }

    private var mapId = 1000000

    inner class ItemViewHolder constructor(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var map: GoogleMap? = null
        private var mapFragment: SupportMapFragment? = null
        private var frameMapId: Int = -1
        private var bindJob: Job? = null

        init {
            addFrameMap()
        }

        fun clear() {
            bindJob?.cancel()
            map?.clear()
            if (binding.containerMap.childCount == 1)
                binding.containerMap.getChildAt(0).hide()
        }

        fun bind(item: HistoryModel) {
            clear()
            bindJob = CoroutineScope(Dispatchers.Default).launch {
                item.session = item.session ?: SharedPrefsStore.getRunningSession(item.time)
                withContext(Dispatchers.Main) {
                    setInfo(item)
                    binding.root.postDelayed(30) {
                        if (map == null)
                            addMapFragment(item.session!!.centerPoint!!) {
                                map = it
                                map?.configGoogleMap(true)
                                binding.root.postDelayed(30) {
                                    showRoutesInMap(item)
                                }
                            }
                        else showRoutesInMap(item)

                    }
                }
            }

            binding.executePendingBindings()

        }

        private fun setInfo(item: HistoryModel) {
            binding.textDistance.text =
                item.session!!.getDistanceMeter().convertMeterToKm().getKmText()
            binding.textAverageSpeed.text =
                item.session!!.getAverageSpeedMS().convertMSToKmH().getKmHText()
            binding.textDuration.text = item.session!!.getDurationMilis().getDurationMilisText()
        }

        private fun showRoutesInMap(item: HistoryModel) {
            item.session!!.routes.forEach { addRoute(it) }
            map?.moveToLocation(item.session!!.centerPoint)
            map?.boundMarkers(item.session!!.getLocations(), animate = false)
            if (binding.containerMap.childCount == 1)
                binding.containerMap.getChildAt(0).show()
        }

        private fun addRoute(route: Route) {
            if (route.firstLocation() != null) {
                map?.addMarkerWithBitmapIcon(route.firstLocation()!!, iconMarkerHead, center = true)
                map?.addMarkerWithBitmapIcon(route.firstLocation()!!, iconMarkerStart, zIndex = 1f)
            }

            if (route.lastLocation() != null) {
                map?.addMarkerWithBitmapIcon(route.lastLocation()!!, iconMarkerHead, center = true)
                map?.addMarkerWithBitmapIcon(route.lastLocation()!!, iconMarkerEnd, zIndex = 2f)
            }
            map?.addPolyline(createPolylineOption(binding.root.context, route.getLocations()))
        }

        private fun addFrameMap() {
            val frameMap = FrameLayout(binding.root.context)
            frameMap.id = mapId++
            frameMap.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            binding.containerMap.addView(frameMap)
            frameMapId = frameMap.id
        }

        private fun getMapFragmentTag() =
            "${SupportMapFragment::class.java.canonicalName ?: SupportMapFragment::class.java.name}_history_adapter_$adapterPosition"

        private fun addMapFragment(point: LatLng, callback: OnMapReadyCallback) {
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance(
                    GoogleMapOptions().liteMode(true)
                        .camera(CameraPosition.Builder().target(point).build())
                )
                fragmentManager?.addFragmentWithTag(
                    getMapFragmentTag(),
                    frameMapId,
                    mapFragment!!
                )
            }
            mapFragment?.getMapAsync(callback)
        }

    }
}