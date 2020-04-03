package team.marker.view.product

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_product.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.model.requests.LoginRequest
import team.marker.model.requests.ProductRequest
import team.marker.view.scan.ScanViewModel

class ProductFragment : Fragment() {
    private lateinit var viewModel: ProductViewModel
    private val product_id: String by lazy { arguments?.get("product_id") as String }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("Message", product_id)
        viewModel.getProduct(product_id)
        btn_back.setOnClickListener { back(view) }

        viewModel.response.observe(viewLifecycleOwner, Observer { response ->
            product_title_info.text = response.title.toString()
            product_code_info.text = response.code.toString()
            company_title_info.text = response.company_title.toString()
            company_address_info.text = response.company_address.toString()
            produced_info.text = response.produced.toString()
        })
    }

    fun back(view: View) {
        Navigation.findNavController(view).navigate(R.id.homeFragment)
    }
}