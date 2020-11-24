package team.marker.view.breach.complete

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_breach_complete.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import team.marker.R
import team.marker.model.requests.BreachRequest

class BreachCompleteFragment : Fragment() {

    private lateinit var viewModel: BreachCompleteViewModel
    private val productIds: ArrayList<String> by lazy { arguments?.get("product_ids") as ArrayList<String> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getSharedViewModel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.dark_gray)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_breach_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for(productId in productIds) {
            Log.e("Message", productId)
        }
        val size = productIds.size
        var productId = 0
        if (productIds.size > 0) productId = productIds[0].toInt()
        /*val labelScan = nameCase(size, arrayOf("Отсканирован", "Отсканировано", "Отсканировано"))
        val labelCode = nameCase(size, arrayOf("код", "кода", "кодов"))
        if (size > 0) note_text.text = "$labelScan $size $labelCode, введите электронную почту для отправки отчета."
        else note_text.text = "Отсканированных кодов нет. Выполните повторное сканирование перед отправкой отчета."
        if (size == 0) {
            input_email.visibility = View.GONE
            btn_send.text = "Закрыть"
            //btn_send.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_logout)
            note_title.setImageResource(R.drawable.ic_empty)
            btn_products.visibility = View.GONE
            ic_email.visibility = View.GONE
        }
        btn_products.setOnClickListener { products(view) }*/
        btn_back.setOnClickListener { back(view) }
        btn_send.setOnClickListener { send(view, productId) }
        add_photo_btn.setOnClickListener { findNavController().navigate(R.id.action_breachCompleteFragment_to_photoFragment) }

        viewModel.photos.observe(viewLifecycleOwner, Observer {
            photos_recycler.setHasFixedSize(true)
            photos_recycler.adapter = PhotosAdapter(it)
            photos_recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        })
    }

    private fun back(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_breachCompleteFragment_to_breachFragment)
    }

    private fun send(view: View, productId: Int) {
        // vars
        val reasonId = 0
        val userReason = ""
        val comment = input_comment.text.toString()
        // validate
        if (comment == "") return
        // send
        viewModel.breach(BreachRequest(productId, reasonId, userReason, comment))
        Navigation.findNavController(view).navigate(R.id.action_breachCompleteFragment_to_homeFragment)
    }

}