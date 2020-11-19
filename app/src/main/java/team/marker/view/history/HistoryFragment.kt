package team.marker.view.history

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.toolbar_history.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R

class HistoryFragment : Fragment() {

    private lateinit var viewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler().postDelayed({
            viewModel.getHistory()

            viewModel.progressIsVisible.observe(viewLifecycleOwner, Observer { visible ->
                progress_bar.visibility = if (visible) View.VISIBLE else View.GONE
            })

            viewModel.response.observe(viewLifecycleOwner, Observer {
                history_recycler.adapter = HistoryAdapter(it.info?: mutableListOf()) { item ->
                    //val bundle = Bundle()
                    //bundle.putString("product_url", item.product?.id?.toString())
                    //NavHostFragment.findNavController(this).navigate(R.id.productFragment, bundle)
                }
            })
        }, 300)


        btn_back.setOnClickListener { back() }
    }

    private fun back() { activity?.onBackPressed() }
}