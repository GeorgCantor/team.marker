package team.marker.view.product

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.toolbar_primary.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.util.ExpandList


class ProductFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: ProductViewModel
    private val product_id: String by lazy { arguments?.get("product_id") as String }

    private lateinit var list1: ExpandList
    private lateinit var list2: ExpandList
    private lateinit var list3: ExpandList
    private lateinit var list4: ExpandList
    private lateinit var list5: ExpandList

    private lateinit var mMap: GoogleMap
    //var mapFragment : SupportMapFragment?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var v = inflater.inflate(R.layout.fragment_product, container, false)

        //mapFragment = fragmentManager?.findFragmentById(R.id.mapView) as SupportMapFragment?
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        //mapFragment.view?.isClickable = false
        mapFragment.getMapAsync(this)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // common
        super.onViewCreated(view, savedInstanceState)
        viewModel.getProduct(product_id)
        list1 = ExpandList(expand_1, list_1_expand)
        list2 = ExpandList(expand_2, list_2_expand)
        list3 = ExpandList(expand_3, list_3_expand)
        list4 = ExpandList(expand_4, list_4_expand)
        list5 = ExpandList(expand_5, list_5_expand)
        btn_back.setOnClickListener { back(view) }
        toggle(list1)
        list_1.setOnClickListener { toggle(list1) }
        list_2.setOnClickListener { toggle(list2) }
        list_3.setOnClickListener { toggle(list3) }
        list_4.setOnClickListener { toggle(list4) }
        list_5.setOnClickListener { toggle(list5) }
        // product info
        viewModel.response.observe(viewLifecycleOwner, Observer { response ->
            if (response.id!! > 0) {
                product_title_info.text = response.title.toString()
                product_code_info.text = response.code.toString()
                company_title_info.text = response.company_title.toString()
                company_address_info.text = response.company_address.toString()
                produced_info.text = response.produced.toString()
            } else {
                NavHostFragment.findNavController(this).navigate(R.id.scanErrorFragment)
            }
        })
    }

    //private var isCollapsed = true
    //private var inProgress = false

    override fun onMapReady(googleMap: GoogleMap) {
        //googleMap.uiSettings.isMapToolbarEnabled = false
        mMap = googleMap
        val marker = LatLng(48.656745, 44.448870)
        mMap.addMarker(MarkerOptions().position(marker).title("Волгограднефтемаш"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f))
        mMap.uiSettings.isScrollGesturesEnabled = false
    }

    private fun toggle(v: ExpandList) {
        v.toggle_list()
    }

    private fun slideAnimator(
        start: Int,
        end: Int,
        summary: View
    ): ValueAnimator? {
        val animator = ValueAnimator.ofInt(start, end)
        animator.addUpdateListener { valueAnimator -> //Update Height
            val value = valueAnimator.animatedValue as Int
            val layoutParams = summary.layoutParams
            layoutParams.height = value
            summary.layoutParams = layoutParams
        }
        return animator
    }

    private fun back(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_productFragment_to_homeFragment)
    }
}