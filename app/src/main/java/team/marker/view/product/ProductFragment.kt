package team.marker.view.product

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import kotlinx.android.synthetic.main.toolbar_primary.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.util.ExpandList
import team.marker.util.shortToast

class ProductFragment : Fragment() {

    private lateinit var viewModel: ProductViewModel
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
        // common
        super.onViewCreated(view, savedInstanceState)
        val productId = productUrl.replace("https://marker.team/products/","")
        viewModel.getProduct(productId)
        // vars
        list1 = ExpandList(expand_1, list_1_expand)
        list2 = ExpandList(expand_2, list_2_expand)
        list3 = ExpandList(expand_3, list_3_expand)
        list4 = ExpandList(expand_4, list_4_expand)
        list5 = ExpandList(expand_5, list_5_expand)
        list1.toggle_list()
        // events
        btn_back.setOnClickListener { back(view) }
        btn_share.setOnClickListener { share(view) }
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

        // product info
        viewModel.response.observe(viewLifecycleOwner, Observer {

            if (it.id!! > 0) {
                // vars
                val manufacturer = it.manufacturer
                val customer = it.customer
                val contract = it.contract
                val customerTitle = customer?.title.toString()
                val customerLat = customer?.address_lat
                val customerLng = customer?.address_lng
                val customerContract = contract?.title.toString() + " от " + contract?.date.toString()
                val customerAnnex = "№" + contract?.annex_number.toString() + " от " + contract?.annex_date.toString()
                val manufacturerTitle = manufacturer?.title.toString()
                val manufacturerLat = manufacturer?.address_lat
                val manufacturerLng = manufacturer?.address_lng
                // texts
                product_title_info.text = it.title.toString()
                product_code_info.text = it.code.toString()
                company_title_info.text = manufacturerTitle
                company_address_info.text = manufacturer?.address.toString()
                company_phone_info.text = manufacturer?.phone.toString()
                company_email_info.text = manufacturer?.email.toString()
                customer_title_info.text = customerTitle
                customer_address_info.text = customer?.address.toString()
                customer_phone_info.text = customer?.phone.toString()
                customer_email_info.text = customer?.email.toString()
                customer_contract_info.text = customerContract
                customer_annex_info.text = customerAnnex
                produced_info.text = it.produced.toString()
                shipped_info.text = it.shipped.toString()
                // map (manufacturer)
                val manufacturerMarker = LatLng(manufacturerLat!!, manufacturerLng!!)
                manufacturerMap.addMarker(MarkerOptions().position(manufacturerMarker).title(manufacturerTitle))
                //manufacturerMap.moveCamera(CameraUpdateFactory.newLatLng(manufacturerMarker))
                //manufacturerMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f))
                manufacturerMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(manufacturerLat!!, manufacturerLng!!), 8f))
                manufacturerMap.uiSettings.isScrollGesturesEnabled = false
                // map (customer)
                val customerMarker = LatLng(customerLat!!, customerLng!!)
                customerMap.addMarker(MarkerOptions().position(customerMarker).title(customerTitle))
                //customerMap.moveCamera(CameraUpdateFactory.newLatLng(customerMarker))
                //customerMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f))
                customerMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(customerLat!!, customerLng!!), 8f))
                customerMap.uiSettings.isScrollGesturesEnabled = false

                product_options_recycler.isNestedScrollingEnabled = false;
                product_options_recycler.adapter = ProductOptionsAdapter(it.options?: mutableListOf()) { option ->
                    //val bundle = Bundle()
                    //bundle.putParcelable("option", option)
                    //NavHostFragment.findNavController(this).navigate(R.id.action_withdrawsFragment_to_withdrawFragment, bundle)
                }

                product_files_recycler.isNestedScrollingEnabled = false;
                product_files_recycler.adapter = ProductFilesAdapter(it.files?: mutableListOf()) { file ->
                    if (file.type == 1) {
                        val intent = Intent(activity, WebViewActivity::class.java)
                        intent.putExtra("path", file.path)
                        intent.putExtra("title", file.title)
                        startActivity(intent)
                    }
                }

                product_options_recycler.measure(View.MeasureSpec.makeMeasureSpec(expand_2.width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST))
                val targetHeight2 = product_options_recycler.measuredHeight
                product_options_recycler.layoutParams.height = targetHeight2

                product_files_recycler.measure(View.MeasureSpec.makeMeasureSpec(expand_5.width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST))
                val targetHeight3 = product_files_recycler.measuredHeight
                product_files_recycler.layoutParams.height = targetHeight3

            } else {
                NavHostFragment.findNavController(this).navigate(R.id.scanErrorFragment)
            }
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

    private fun share(view: View) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL")
        i.putExtra(Intent.EXTRA_TEXT, "http://www.url.com")
        startActivity(Intent.createChooser(i, "Share URL"))
    }

    private fun makeCall() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:+$phone")

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 10)
            return
        } else {
            try {
                startActivity(intent)
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
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
        //intent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
        //intent.putExtra(Intent.EXTRA_TEXT, "Body")
        startActivity(Intent.createChooser(intent, ""))
    }
}