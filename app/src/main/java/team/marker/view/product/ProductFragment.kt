package team.marker.view.product

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.MeasureSpec.*
import android.view.View.VISIBLE
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.toolbar_product.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.util.*
import team.marker.util.Constants.FORCE
import team.marker.util.Constants.LATITUDE
import team.marker.util.Constants.LONGITUDE
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.PRODUCTS_URL
import team.marker.util.Constants.PRODUCT_URL

class ProductFragment : Fragment(R.layout.fragment_product) {

    private val viewModel by inject<ProductViewModel>()
    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))
    private val productUrl: String by lazy { arguments?.get(PRODUCT_URL) as String }

    private lateinit var manufacturerMap: GoogleMap
    private lateinit var customerMap: GoogleMap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = productUrl.replace(PRODUCTS_URL, "")

        (childFragmentManager.findFragmentById(R.id.manufacturer_map) as SupportMapFragment).apply {
            getMapAsync(onMapReadyCallback1())
        }
        (childFragmentManager.findFragmentById(R.id.customer_map) as SupportMapFragment).apply {
            getMapAsync(onMapReadyCallback2())
        }

        val lat = preferences.getAny("", LATITUDE) as String
        val lng = preferences.getAny("", LONGITUDE) as String

        viewModel.getProduct(productId, lat, lng)

        val list1 = ExpandList(expand_1, list_1_expand)
        val list2 = ExpandList(expand_2, list_2_expand)
        val list3 = ExpandList(expand_3, list_3_expand)
        val list4 = ExpandList(expand_4, list_4_expand)
        val list5 = ExpandList(expand_5, list_5_expand)
        list1.toggleList(FORCE)

        btn_back.setOnClickListener { activity?.onBackPressed() }
        list_1.setOnClickListener { list1.toggleList() }
        list_2.setOnClickListener { list2.toggleList() }
        list_3.setOnClickListener { list3.toggleList() }
        list_4.setOnClickListener { list4.toggleList() }
        list_5.setOnClickListener {
            list_5.parent.requestChildFocus(list_5, list_5)
            list5.toggleList()
        }

        viewModel.error.observe(viewLifecycleOwner, {
            findNavController().navigate(R.id.action_productFragment_to_scanErrorFragment)
        })

        viewModel.response.observe(viewLifecycleOwner, {
            val manufacturer = it.manufacturer
            val customer = it.customer
            val consignee = it.consignee
            val contract = it.contract
            val productCode = it.code.toString()
            val productTitle = it.title.toString()
            val customerTitle = if (customer?.title.isNullOrBlank()) getString(R.string.not_specified) else customer?.title
            val customerAddress = if (customer?.address.isNullOrBlank()) getString(R.string.not_specified) else customer?.address
            val customerLat = customer?.address_lat
            val customerLng = customer?.address_lng
            val customerContract = if (contract?.title.isNullOrBlank()) getString(R.string.not_specified) else "${contract?.title.toString()} от ${contract?.date.toString()}"
            val customerAnnex = if (contract?.annex_number.isNullOrBlank()) getString(R.string.not_specified2) else "№${contract?.annex_number.toString()} от ${contract?.annex_date.toString()}"
            val consigneeTitle = if (consignee?.title.isNullOrBlank()) getString(R.string.not_specified) else consignee?.title
            val consigneeAddress = if (consignee?.address.isNullOrBlank()) getString(R.string.not_specified) else consignee?.address
            val consigneeDestination = if (it.destination.isNullOrBlank()) getString(R.string.not_specified) else it.destination.toString()
            val manufacturerTitle = manufacturer?.title
            val manufacturerLat = manufacturer?.address_lat
            val manufacturerLng = manufacturer?.address_lng
            val producedDate = if (it.produced.isNullOrBlank()) getString(R.string.not_specified3) else it.produced.toString()
            val shippedDate = if (it.shipped.isNullOrBlank()) getString(R.string.not_specified3) else it.shipped.toString()

            product_title_info.text = productTitle
            product_code_info.text = productCode
            company_title_info.text = manufacturerTitle
            company_address_info.text = manufacturer?.address.toString()
            company_phone_info.text = manufacturer?.phone.toString()
            customer_title_info.text = customerTitle
            customer_address_info.text = customerAddress
            consignee_title_info.text = consigneeTitle
            consignee_address_info.text = consigneeAddress
            consignee_destination_info.text = consigneeDestination
            customer_contract_info.text = customerContract
            customer_annex_info.text = customerAnnex
            produced_info.text = producedDate
            shipped_info.text = shippedDate

            if (productCode == getString(R.string.not_specified)) product_code.gone()
            if (producedDate == getString(R.string.not_specified3)) produced.gone()
            if (shippedDate == getString(R.string.not_specified3)) shipped.gone()

            val manufacturerMarker = LatLng(manufacturerLat!!, manufacturerLng!!)
            manufacturerMap.addMarker(MarkerOptions().position(manufacturerMarker).title(manufacturerTitle))
            manufacturerMap.moveCamera(CameraUpdateFactory.newLatLngZoom(manufacturerMarker, 8F))
            manufacturerMap.uiSettings.isScrollGesturesEnabled = false

            when (customerLat.toString()) {
                "0.0" -> customer_map_wrap.gone()
                else -> {
                    val customerMarker = LatLng(customerLat!!, customerLng!!)
                    customerMap.addMarker(MarkerOptions().position(customerMarker).title(customerTitle))
                    customerMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerMarker, 8F))
                    customerMap.uiSettings.isScrollGesturesEnabled = false
                }
            }

            when {
                it.options?.size!! > 0 -> {
                    product_options_recycler.isNestedScrollingEnabled = false
                    product_options_recycler.adapter = ProductOptionsAdapter(it.options)
                }
                else -> expand_2_empty.visible()
            }

            when (it.files?.isNotEmpty()) {
                true -> {
                    product_files_recycler.isNestedScrollingEnabled = false
                    product_files_recycler.adapter = ProductFilesAdapter(it.files) { file ->
                        file.path?.let { openDocument(it) }
                    }
                }
                false -> expand_5_empty.visible()
            }

            product_options_recycler.measure(makeMeasureSpec(expand_2.width, EXACTLY), makeMeasureSpec(0, AT_MOST))
            val targetHeight2 = product_options_recycler.measuredHeight
            product_options_recycler.layoutParams.height = targetHeight2

            product_files_recycler.measure(makeMeasureSpec(expand_5.width, EXACTLY), makeMeasureSpec(0, AT_MOST))
            val targetHeight3 = product_files_recycler.measuredHeight
            product_files_recycler.layoutParams.height = targetHeight3

            btn_share.setOnClickListener { share(productUrl, productTitle) }
        })

        viewModel.progressIsVisible.observe(viewLifecycleOwner) {
            root_layout.visibility = if (it) GONE else VISIBLE
            200L.runDelayed { progress_bar.isVisible = it }
        }
    }

    private fun onMapReadyCallback1() = OnMapReadyCallback { map -> manufacturerMap = map }

    private fun onMapReadyCallback2() = OnMapReadyCallback { map -> customerMap = map }

    private fun openDocument(filePath: String) {
        CustomTabsIntent.Builder().apply {
            setToolbarColor(getColor(requireContext(), R.color.dark_blue))
            setStartAnimations(requireContext(), R.anim.slide_in_right, R.anim.slide_out_left)
            setExitAnimations(requireContext(), R.anim.slide_in_left, R.anim.slide_out_right)
        }.build().launchUrl(requireContext(), Uri.parse(filePath))
    }

    private fun share(url: String, title: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, title)
        i.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(Intent.createChooser(i, "Поделиться URL"))
    }
}