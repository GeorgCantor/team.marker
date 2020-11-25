package team.marker.view.pick.products

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.toolbar_history.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R

class PickProductsFragment : Fragment(R.layout.fragment_pick_products) {

    private lateinit var viewModel: PickProductsViewModel
    private val productIds: String by lazy { arguments?.get("product_ids") as String }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.dark_gray)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProducts(productIds)

        viewModel.progressIsVisible.observe(viewLifecycleOwner, Observer { visible ->
            progress_bar.visibility = if (visible) View.VISIBLE else View.GONE
        })

        viewModel.response.observe(viewLifecycleOwner, Observer {
            history_recycler.adapter = PickProductsAdapter(it.info ?: mutableListOf()) { item ->
                Handler().postDelayed({
                    val bundle = Bundle()
                    bundle.putString(
                        "product_url",
                        "https://marker.team/products/" + item.id?.toString()
                    )
                    NavHostFragment.findNavController(this)
                        .navigate(R.id.action_pickProductsFragment_to_productFragment, bundle)
                }, 150)
            }
        })

        btn_back.setOnClickListener { back() }
    }

    private fun back() {
        activity?.onBackPressed()
    }

}