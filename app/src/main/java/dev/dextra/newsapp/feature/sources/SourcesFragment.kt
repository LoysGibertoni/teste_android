package dev.dextra.newsapp.feature.sources

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textfield.TextInputLayout
import dev.dextra.newsapp.R
import dev.dextra.newsapp.api.model.Source
import dev.dextra.newsapp.api.model.enums.Category
import dev.dextra.newsapp.api.model.enums.Country
import dev.dextra.newsapp.base.BaseListFragment
import dev.dextra.newsapp.feature.sources.adapter.CustomArrayAdapter
import dev.dextra.newsapp.feature.sources.adapter.SourcesListAdapter
import kotlinx.android.synthetic.main.fragment_sources.*
import org.koin.android.viewmodel.ext.android.viewModel

class SourcesFragment : BaseListFragment(), SourcesListAdapter.SourceListAdapterItemListener {

    override val emptyStateTitle: Int = R.string.empty_state_title_source
    override val emptyStateSubTitle: Int = R.string.empty_state_subtitle_source
    override val errorStateTitle: Int = R.string.error_state_title_source
    override val errorStateSubTitle: Int = R.string.error_state_subtitle_source
    override val mainList: View
        get() = sources_list

    private val sourcesViewModel: SourcesViewModel by viewModel()

    private var viewAdapter: SourcesListAdapter = SourcesListAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadSources()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sources, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    private fun setupList() {
        sources_list.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
        }
    }

    private fun loadSources() {
        sourcesViewModel.sources.observe(this, Observer {
            viewAdapter.apply {
                clear()
                notifyDataSetChanged()
                add(it)
                notifyDataSetChanged()
                sources_list.scrollToPosition(0)
                app_bar.setExpanded(true)
            }
        })

        sourcesViewModel.networkState.observe(this, networkStateObserver)

        sourcesViewModel.loadSources()
    }

    private fun setupView() {
        configureAutocompletes()
        setupList()
    }

    private fun configureAutocompletes() {
        val countryAdapter = CustomArrayAdapter(
            requireContext(),
            R.layout.select_item,
            Country.values().toMutableList()
        )
        country_select.setAdapter(countryAdapter)
        country_select.keyListener = null
        country_select.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position)
            if (item is Country) {
                sourcesViewModel.changeCountry(item)
            }
        }

        category_select.setAdapter(
            CustomArrayAdapter(
                requireContext(),
                R.layout.select_item,
                Category.values().toMutableList()
            )
        )
        category_select.keyListener = null
        category_select.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position)
            if (item is Category) {
                sourcesViewModel.changeCategory(item)
            }
        }
    }

    override fun onClick(source: Source) {
        val directions = SourcesFragmentDirections.navigateToNews(source)
        findNavController().navigate(directions)
    }

    override fun setupPortrait() {
        setListColumns(1)
        sources_filters.orientation = LinearLayout.VERTICAL
        configureFilterLayoutParams(country_select_layout, ViewGroup.LayoutParams.MATCH_PARENT, 0f)
        configureFilterLayoutParams(category_select_layout, ViewGroup.LayoutParams.MATCH_PARENT, 0f)
    }

    override fun setupLandscape() {
        setListColumns(2)
        sources_filters.orientation = LinearLayout.HORIZONTAL
        configureFilterLayoutParams(country_select_layout, 0, 1f)
        configureFilterLayoutParams(category_select_layout, 0, 1f)
    }

    private fun configureFilterLayoutParams(textInput: TextInputLayout, width: Int, weight: Float) {
        val layoutParams = textInput.layoutParams
        if (layoutParams is LinearLayout.LayoutParams) {
            layoutParams.width = width
            layoutParams.weight = weight
        }
    }

    private fun setListColumns(columns: Int) {
        val layoutManager = sources_list.layoutManager
        if (layoutManager is GridLayoutManager) {
            layoutManager.spanCount = columns
            viewAdapter.notifyDataSetChanged()
        }
    }

    override fun executeRetry() {
        loadSources()
    }
}