package co.ke.tonyoa.nytimesnews.ui.list

import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import co.ke.tonyoa.nytimesnews.R
import co.ke.tonyoa.nytimesnews.databinding.FragmentItemListBinding
import co.ke.tonyoa.nytimesnews.databinding.LayoutSortBinding
import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.models.OrderBy
import co.ke.tonyoa.nytimesnews.domain.models.OrderDirection
import co.ke.tonyoa.nytimesnews.ui.detail.ItemDetailFragmentDirections
import co.ke.tonyoa.nytimesnews.utils.DataState
import co.ke.tonyoa.nytimesnews.utils.Utils.createRandomNews
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview

@FlowPreview
@AndroidEntryPoint
class ItemListFragment : Fragment() {

    private lateinit var binding: FragmentItemListBinding

    private val itemListViewModel: ItemListViewModel by viewModels()
    private lateinit var newsAdapter: NewsAdapter
    private var lastBackClick: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentItemListBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.layoutToolbar.materialToolbar)
        setupDrawerLayout()

        newsAdapter = NewsAdapter { _, news ->
            itemListViewModel.performEvent(
                ItemListViewModel.ListUiEvent.ViewNews(
                    news.id,
                    binding.itemDetailNavContainer != null
                )
            )
        }
        binding.recyclerView.adapter = newsAdapter

        binding.swipeRefreshLayout.setOnRefreshListener(this::performQuery)
        binding.layoutFailureState.buttonFailureRetry.setOnClickListener { performQuery() }
        binding.layoutEmptyState.buttonEmptyRetry.setOnClickListener { performQuery() }

        itemListViewModel.listUiState.observe(viewLifecycleOwner, {
            // If a news item is selected in tablet
            binding.itemDetailNavContainer?.visibility = it.viewedNews?.let { VISIBLE } ?: GONE

            when (val result = it.result) {
                is DataState.Success -> {
                    showSuccess(result)
                }
                is DataState.Failure -> {
                    showFailure(result)
                }
                is DataState.Loading -> {
                    startLoading(result)
                }
            }
        })
        itemListViewModel.singleEventState.observe(viewLifecycleOwner, {
            it?.let {
                when (it) {
                    is ItemListViewModel.ListSingleEvent.ShowSnackbar -> {
                        Snackbar.make(binding.recyclerView, it.message, LENGTH_LONG).show()
                    }
                    is ItemListViewModel.ListSingleEvent.ViewDetail -> {
                        navigateToDetail(it.id)
                    }
                }
                itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ConsumeSingleEvent)
            }
        })

        return binding.root

    }

    private fun navigateToDetail(id: Long) {
        // Tablet Screen
        if (binding.itemDetailNavContainer != null) {
            binding.itemDetailNavContainer?.apply {
                findNavController().navigate(
                    ItemDetailFragmentDirections.actionFragmentItemDetailSelf(
                        id
                    )
                )
            }
        }
        // Small screen
        else {
            findNavController().navigate(
                ItemListFragmentDirections.showItemDetail(id)
            )
        }
    }

    private fun performQuery() {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Refresh)
    }

    private fun startLoading(result: DataState.Loading<List<News>>) {
        val resultValues = result.data

        // If the loading had some data, display the new list, this will take care of
        // hiding and showing empty state for list
        if (resultValues != null) {
            displayList(resultValues)
        }

        binding.linearProgressIndicator.visibility = VISIBLE
        binding.swipeRefreshLayout.isRefreshing = true
        // Prevent calls to refresh while loading
        binding.layoutEmptyState.buttonEmptyRetry.isEnabled = false
        binding.layoutFailureState.buttonFailureRetry.isEnabled = false
        binding.swipeRefreshLayout.isEnabled = false
    }

    private fun stopLoading() {
        binding.linearProgressIndicator.visibility = GONE
        binding.swipeRefreshLayout.isRefreshing = false
        // Re-enable calls to refresh
        binding.layoutEmptyState.buttonEmptyRetry.isEnabled = true
        binding.layoutFailureState.buttonFailureRetry.isEnabled = true
        binding.swipeRefreshLayout.isEnabled = true
    }

    private fun showSuccess(dataState: DataState.Success<List<News>>) {
        val list = dataState.data
        displayList(list)
        binding.layoutFailureState.root.visibility = GONE
        stopLoading()
    }

    private fun showFailure(result: DataState.Failure<List<News>>) {
        val message = result.throwable.message
        val resultValues = result.data

        // If the failure had some data, display the new list, this will take care of
        // hiding and showing empty state for list
        var itemCount = newsAdapter.itemCount
        if (resultValues != null) {
            displayList(resultValues)
            itemCount = resultValues.size
        }

        // An existing list is being displayed, hide empty state
        if (itemCount > 0) {
            binding.layoutFailureState.textViewFailureMessage.text = message
            binding.layoutEmptyState.root.visibility = GONE
            binding.layoutFailureState.root.visibility = VISIBLE
        }
        // No list to display, show empty state with error message
        else {
            binding.layoutEmptyState.textViewEmptyText.text = message
            binding.layoutEmptyState.root.visibility = VISIBLE
            binding.layoutFailureState.root.visibility = GONE
        }
        stopLoading()
    }

    private fun displayList(list: List<News>?) {
        newsAdapter.submitList(list)
        if (list == null || list.isEmpty()) {
            binding.recyclerView.visibility = GONE
            binding.layoutEmptyState.root.visibility = VISIBLE
            binding.layoutEmptyState.textViewEmptyText.text =
                getString(R.string.no_news_items_found)
        } else {
            binding.recyclerView.visibility = VISIBLE
            binding.layoutEmptyState.root.visibility = GONE
        }
    }

    private fun setupDrawerLayout() {
        val itemListContainer = binding.itemListContainer
        val navigationView = binding.layoutNavigationDrawer.navigationView
        binding.layoutToolbar.materialToolbar.setNavigationOnClickListener {
            itemListContainer.openDrawer(
                navigationView
            )
        }
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        itemListContainer.isDrawerOpen(navigationView) -> {
                            itemListContainer.closeDrawer(navigationView)
                        }
                        // Pressed back twice
                        (lastBackClick + BACK_DELAY) > System.currentTimeMillis() -> {
                            activity?.finish()
                        }
                        else -> {
                            Toast.makeText(
                                context,
                                getString(R.string.press_back_again),
                                LENGTH_SHORT
                            ).show()
                            lastBackClick = System.currentTimeMillis()
                        }
                    }
                }
            })
        navigationView.setNavigationItemSelectedListener {
            itemListContainer.closeDrawer(navigationView)

            when (it.itemId) {
                R.id.nav_drawer_share -> {
                    val shareIntent = ShareCompat
                        .IntentBuilder(requireContext())
                        .setText(getString(R.string.share_message))
                        .setType("text/*")
                        .createChooserIntent()

                    requireContext().packageManager.resolveActivity(shareIntent, MATCH_DEFAULT_ONLY)
                        ?.apply {
                            requireContext().startActivity(shareIntent)
                        }
                    true
                }
                R.id.nav_drawer_create_news -> {
                    val news = createRandomNews()
                    itemListViewModel
                        .performEvent(
                            ItemListViewModel.ListUiEvent.CreateNews(
                                news.title,
                                news.newsAbstract,
                                news.publishDate,
                                news.category,
                                news.author,
                                news.source,
                                news.url,
                                news.images
                            )
                        )
                    true
                }
                else -> {
                    true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_options_menu, menu)

        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_news)
        searchView.setOnCloseListener {
            itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search(null))
            false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search(newText))
                return true
            }
        })

        val query = itemListViewModel.listUiState.value?.query
        if (query != null) {
            searchView.isIconified = false
            searchView.setIconifiedByDefault(false)
            searchItem.expandActionView()
            searchView.requestFocus()
            searchView.setQuery(query, false)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                sortNewsItems()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sortNewsItems() {
        val layoutSortBinding = LayoutSortBinding.inflate(layoutInflater, null, false).apply {
            val orderBy = itemListViewModel.listUiState.value?.orderBy ?: OrderBy.Date()

            when (orderBy) {
                is OrderBy.Title -> radioButtonTitle.isChecked = true
                is OrderBy.Date -> radioButtonDate.isChecked = true
                is OrderBy.Category -> radioButtonCategory.isChecked = true
                is OrderBy.Author -> radioButtonAuthor.isChecked = true
                is OrderBy.Source -> radioButtonSource.isChecked = true
            }
            when (orderBy.orderDirection) {
                OrderDirection.ASCENDING -> radioButtonAscending.isChecked = true
                OrderDirection.DESCENDING -> radioButtonDescending.isChecked = true
            }
        }

        AlertDialog.Builder(requireContext())
            .setView(layoutSortBinding.root)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.sort) { dialog, _ ->
                val sort = when (layoutSortBinding.radioGroupColumns.checkedRadioButtonId) {
                    R.id.radioButton_title -> "title"
                    R.id.radioButton_category -> "category"
                    R.id.radioButton_author -> "author"
                    R.id.radioButton_source -> "source"
                    else -> "date"
                }
                val ascending =
                    layoutSortBinding.radioGroupDirection.checkedRadioButtonId == R.id.radioButton_ascending
                itemListViewModel.performEvent(
                    ItemListViewModel.ListUiEvent.Sort(
                        sort,
                        ascending
                    )
                )
                dialog.dismiss()
            }
            .create()
            .show()
    }

    companion object {
        const val BACK_DELAY = 2000L
    }
}