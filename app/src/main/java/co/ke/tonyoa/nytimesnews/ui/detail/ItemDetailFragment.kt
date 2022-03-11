package co.ke.tonyoa.nytimesnews.ui.detail

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.PagerSnapHelper
import co.ke.tonyoa.nytimesnews.R
import co.ke.tonyoa.nytimesnews.databinding.FragmentItemDetailBinding
import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.utils.DataState
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ItemDetailFragment : Fragment() {

    private lateinit var binding: FragmentItemDetailBinding
    private val itemDetailViewModel: ItemDetailViewModel by viewModels()
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var newsImageImageAdapter: NewsImageImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentItemDetailBinding.inflate(inflater, container, false)

        if (savedInstanceState == null && arguments != null && arguments!!.size() > 0) {
            itemDetailViewModel
                .performEvent(
                    ItemDetailViewModel
                        .DetailUiEvent
                        .GetNews(ItemDetailFragmentArgs.fromBundle(arguments ?: Bundle()).itemId)
                )
        }

        // If is mobile, without this options menu in list for tablet does not show because it is overridden
        if (resources.getInteger(R.integer.detail_toolbar_visibility) == 0)
            (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        newsImageImageAdapter = NewsImageImageAdapter { _, _ ->

        }
        binding.recyclerViewImages.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        binding.recyclerViewImages.adapter = newsImageImageAdapter
        object : PagerSnapHelper() {}.attachToRecyclerView(binding.recyclerViewImages)

        binding.layoutEmptyState.buttonEmptyRetry.visibility = GONE

        itemDetailViewModel.uiState.observe(viewLifecycleOwner, {
            when (it) {
                is DataState.Success -> successfulLoad(it)
                is DataState.Failure -> failedLoad(it)
                is DataState.Loading -> startLoading(it)
            }
        })

        return binding.root
    }

    private fun startLoading(dataState: DataState.Loading<News?>) {
        startLoadingProgress()
        if (dataState.data != null) {
            displayNews(dataState.data)
        }
    }

    private fun startLoadingProgress() {
        binding.linearProgressIndicator.visibility = VISIBLE
        binding.layoutFailureState.buttonFailureRetry.isEnabled = false
    }

    private fun stopLoadingProgress() {
        binding.linearProgressIndicator.visibility = GONE
        binding.layoutFailureState.buttonFailureRetry.isEnabled = true
    }

    private fun failedLoad(dataState: DataState.Failure<News?>) {
        stopLoadingProgress()
        if (dataState.data != null) {
            binding.layoutFailureState.textViewFailureMessage.text = dataState.throwable.message
            binding.layoutFailureState.root.visibility = VISIBLE
            displayNews(dataState.data)
        } else {
            binding.layoutEmptyState.textViewEmptyText.text = dataState.throwable.message
            binding.layoutFailureState.root.visibility = GONE
            binding.layoutEmptyState.root.visibility = VISIBLE
            binding.linearLayoutDetails.visibility = GONE
        }
    }

    private fun successfulLoad(dataState: DataState.Success<News?>) {
        stopLoadingProgress()
        if (dataState.data != null) {
            displayNews(dataState.data)
        } else {
            binding.layoutEmptyState.textViewEmptyText.text = getString(R.string.select_news_item)
            binding.layoutEmptyState.root.visibility = VISIBLE
            binding.linearLayoutDetails.visibility = GONE
        }
        binding.layoutFailureState.root.visibility = GONE
    }

    private fun displayNews(news: News) {
        newsImageImageAdapter.submitList(news.images)
        binding.apply {
            layoutEmptyState.root.visibility = GONE
            linearLayoutDetails.visibility = VISIBLE
            if (news.images.isEmpty()) {
                recyclerViewImages.visibility = GONE
                collapsingToolbarLayout.isTitleEnabled = true
            } else {
                recyclerViewImages.visibility = VISIBLE
                collapsingToolbarLayout.isTitleEnabled = false
            }

            toolbar.title = news.title
            textViewTitle.text = news.title
            textViewDate.text = simpleDateFormat.format(news.publishDate)
            textViewCategory.text = news.category
            textViewAbstract.text = news.newsAbstract
            textViewAuthor.text = news.author
            textViewSource.text = news.source

            webView.webViewClient = object : WebViewClient() {
                private var hasError = false
                private fun setHasError(errorDescription: String?) {
                    // Don't display errors for disabling of JavaScript
                    if (errorDescription == null || !errorDescription.contains("ERR_BLOCKED_BY_RESPONSE")) {
                        hasError = true
                        stopLoadingProgress()
                        layoutFailureState.textViewFailureMessage.text =
                            getString(R.string.error_loading_web)
                        layoutFailureState.root.visibility = VISIBLE
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    startLoadingProgress()
                    hasError = false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    stopLoadingProgress()
                    layoutFailureState.root.visibility = if (hasError) VISIBLE else GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    setHasError(description.toString())
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setHasError(error?.description.toString())
                    }
                }
            }
            webView.loadUrl(news.url)
            binding.layoutFailureState.buttonFailureRetry.setOnClickListener {
                webView.loadUrl(news.url)
            }
        }
    }

}