package team.marker.view.product

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.toolbar_product.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.util.Constants
import team.marker.util.ExpandList
import team.marker.util.PreferenceManager
import team.marker.util.shortToast

class ProductFragment : Fragment() {

    private lateinit var viewModel: ProductViewModel
    private lateinit var prefManager: PreferenceManager
    private val productUrl: String by lazy { arguments?.get("product_url") as String }

    private var phone: String? = null

    private lateinit var list1: ExpandList
    private lateinit var list2: ExpandList
    private lateinit var list3: ExpandList
    private lateinit var list4: ExpandList
    private lateinit var list5: ExpandList

    private lateinit var manufacturerMap: GoogleMap
    private lateinit var customerMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
        prefManager = PreferenceManager(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate
        val v = inflater.inflate(R.layout.fragment_product, container, false)
        // map (manufacturer)
        val manufacturer = childFragmentManager.findFragmentById(R.id.manufacturer_map) as SupportMapFragment
        manufacturer.getMapAsync(onMapReadyCallback1())
        // map (customer)
        val customer = childFragmentManager.findFragmentById(R.id.customer_map) as SupportMapFragment
        customer.getMapAsync(onMapReadyCallback2())
        // output
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // override
        super.onViewCreated(view, savedInstanceState)
        // common
        val productId = productUrl.replace("https://marker.team/products/","")
        val lat = prefManager.getString("lat") ?: ""
        val lng = prefManager.getString("lng") ?: ""
        viewModel.getProduct(productId, lat, lng)
        // vars
        list1 = ExpandList(expand_1, list_1_expand)
        list2 = ExpandList(expand_2, list_2_expand)
        list3 = ExpandList(expand_3, list_3_expand)
        list4 = ExpandList(expand_4, list_4_expand)
        list5 = ExpandList(expand_5, list_5_expand)
        list1.toggle_list()
        // events
        btn_back.setOnClickListener { back(view) }
        list_1.setOnClickListener { list1.toggle_list() }
        list_2.setOnClickListener { list2.toggle_list() }
        list_3.setOnClickListener { list3.toggle_list() }
        list_4.setOnClickListener { list4.toggle_list() }
        list_5.setOnClickListener {
            list_5.parent.requestChildFocus(list_5, list_5)
            list5.toggle_list()
        }

        company_phone_info.setOnClickListener {
            phone = company_phone_info.text.toString()
            phone = phone?.replace("[^\\d]+", "")
            makeCall()
        }

        customer_phone_info.setOnClickListener {
            phone = customer_phone_info.text.toString()
            phone = phone?.replace("[^\\d]+", "")
            makeCall()
        }

        company_email_info.setOnClickListener {
            val email = company_email_info.text.toString()
            sendEmail(email)
        }

        customer_email_info.setOnClickListener {
            val email = customer_email_info.text.toString()
            sendEmail(email)
        }

        viewModel.error.observe(viewLifecycleOwner, Observer {
            //Log.e("message", "not found")
            NavHostFragment.findNavController(this).navigate(R.id.scanErrorFragment)
        })

        // product info
        viewModel.response.observe(viewLifecycleOwner, Observer {
            // vars
            val manufacturer = it.manufacturer
            val customer = it.customer
            val consignee = it.consignee
            val contract = it.contract
            val productCode = it.code.toString()
            val productTitle = it.title.toString()
            val customerTitle = if (customer?.title.isNullOrBlank()) "не указан" else customer?.title
            val customerAddress = if (customer?.address.isNullOrBlank()) "не указан" else customer?.address
            val customerPhone = if (customer?.phone.isNullOrBlank()) "не указан" else customer?.phone
            val customerEmail = if (customer?.email.isNullOrBlank()) "не указана" else customer?.email
            val customerLat = customer?.address_lat
            val customerLng = customer?.address_lng
            val customerContract = if (contract?.title.isNullOrBlank()) "не указан" else contract?.title.toString() + " от " + contract?.date.toString()
            val customerAnnex = if (contract?.annex_number.isNullOrBlank()) "не указано" else "№" + contract?.annex_number.toString() + " от " + contract?.annex_date.toString()
            val consigneeTitle = if (consignee?.title.isNullOrBlank()) "не указан" else consignee?.title
            val consigneeAddress = if (consignee?.address.isNullOrBlank()) "не указан" else consignee?.address
            val consigneeDestination = if (it.destination.isNullOrBlank()) "не указан" else it.destination.toString()
            val manufacturerTitle = manufacturer?.title
            val manufacturerLat = manufacturer?.address_lat
            val manufacturerLng = manufacturer?.address_lng
            val producedDate = if (it.produced.isNullOrBlank()) "не указана" else it.produced.toString()
            val shippedDate = if (it.shipped.isNullOrBlank()) "не указана" else it.shipped.toString()
            // texts
            product_title_info.text = productTitle
            product_code_info.text = productCode
            company_title_info.text = manufacturerTitle
            company_address_info.text = manufacturer?.address.toString()
            company_phone_info.text = manufacturer?.phone.toString()
            company_email_info.text = manufacturer?.email.toString()
            customer_title_info.text = customerTitle
            customer_address_info.text = customerAddress
            consignee_title_info.text = consigneeTitle
            consignee_address_info.text = consigneeAddress
            consignee_destination_info.text = consigneeDestination
            customer_phone_info.text = customerPhone
            customer_email_info.text = customerEmail
            customer_contract_info.text = customerContract
            customer_annex_info.text = customerAnnex
            produced_info.text = producedDate
            shipped_info.text = shippedDate
            // hide empty
            if (productCode == "не указан") product_code.visibility = View.GONE
            if (producedDate == "не указана") produced.visibility = View.GONE
            if (shippedDate == "не указана") shipped.visibility = View.GONE
            // map (manufacturer)
            val manufacturerMarker = LatLng(manufacturerLat!!, manufacturerLng!!)
            manufacturerMap.addMarker(MarkerOptions().position(manufacturerMarker).title(manufacturerTitle))
            manufacturerMap.moveCamera(CameraUpdateFactory.newLatLngZoom(manufacturerMarker, 8f))
            manufacturerMap.uiSettings.isScrollGesturesEnabled = false
            // map (customer)
            if (customerLat.toString() == "0.0") {
                customer_map_wrap.visibility = View.GONE
            } else {
                val customerMarker = LatLng(customerLat!!, customerLng!!)
                customerMap.addMarker(MarkerOptions().position(customerMarker).title(customerTitle))
                customerMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerMarker, 8f))
                customerMap.uiSettings.isScrollGesturesEnabled = false
            }

            if (it.options?.size!! > 0) {
                product_options_recycler.isNestedScrollingEnabled = false;
                product_options_recycler.adapter = ProductOptionsAdapter(it.options ?: mutableListOf()) { }
            } else {
                expand_2_empty.visibility = View.VISIBLE
            }

            if (it.files?.size!! > 0) {
                product_files_recycler.isNestedScrollingEnabled = false;
                product_files_recycler.adapter = ProductFilesAdapter(it.files ?: mutableListOf()) { file ->
                    if (file.type == 1) {
                        val intent = Intent(activity, WebViewActivity::class.java)
                        intent.putExtra("path", file.path)
                        intent.putExtra("title", file.title)
                        startActivity(intent)
                        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            } else {
                expand_5_empty.visibility = View.VISIBLE
            }

            product_options_recycler.measure(View.MeasureSpec.makeMeasureSpec(expand_2.width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST))
            val targetHeight2 = product_options_recycler.measuredHeight
            product_options_recycler.layoutParams.height = targetHeight2

            product_files_recycler.measure(View.MeasureSpec.makeMeasureSpec(expand_5.width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST))
            val targetHeight3 = product_files_recycler.measuredHeight
            product_files_recycler.layoutParams.height = targetHeight3

            btn_share.setOnClickListener { share(productUrl, productTitle) }
        })
    }

    private fun onMapReadyCallback1(): OnMapReadyCallback? {
        return OnMapReadyCallback { googleMap ->
            manufacturerMap = googleMap
        }
    }

    private fun onMapReadyCallback2(): OnMapReadyCallback? {
        return OnMapReadyCallback { googleMap ->
            customerMap = googleMap
        }
    }

    private fun back(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_productFragment_to_homeFragment)
    }

    private fun share(url: String, title: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, title)
        i.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(Intent.createChooser(i, "Поделиться URL"))
    }

    private fun makeCall() {
        // vars
        val i = Intent(Intent.ACTION_CALL)
        i.data = Uri.parse("tel:+$phone")
        // actions
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 10)
            return
        } else {
            try {
                startActivity(i)
            } catch (ex: ActivityNotFoundException) {
                requireActivity().shortToast("приложение для звонков не найдено")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makeCall()
        }
    }

    private fun sendEmail(email: String) {
        val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
        startActivity(Intent.createChooser(i, "Отправить email"))
    }
}