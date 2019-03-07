package io.onedev.server.web.page.project.blob.render.renderers.cispec.dependencies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Preconditions;

import io.onedev.server.ci.DependencySpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.SideFloating;
import jersey.repackaged.com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class DependencyListViewPanel extends Panel {

	private final List<DependencySpec> dependencies = new ArrayList<>();
	
	public DependencyListViewPanel(String id, Class<?> elementClass, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			dependencies.add((DependencySpec) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<DependencySpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<DependencySpec, Void>(Model.of("Job")) {

			@Override
			public void populateItem(Item<ICellPopulator<DependencySpec>> cellItem, String componentId, IModel<DependencySpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getJob());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<DependencySpec, Void>(Model.of("Artifacts")) {

			@Override
			public void populateItem(Item<ICellPopulator<DependencySpec>> cellItem, String componentId, IModel<DependencySpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel) {

					@Override
					protected Component newLabel(String componentId) {
						String artifacts = rowModel.getObject().getArtifacts();
						if (StringUtils.isNotBlank(artifacts))
							return new Label(componentId, artifacts);
						else
							return new Label(componentId, "<i>Not specified</i>").setEscapeModelStrings(false);
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<DependencySpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<DependencySpec>> cellItem, String componentId, IModel<DependencySpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, "<i class='fa fa-ellipsis-h'></i>").setEscapeModelStrings(false);
					}
					
				});
			}

			@Override
			public String getCssClass() {
				return "ellipsis";
			}
			
		});		
		
		IDataProvider<DependencySpec> dataProvider = new ListDataProvider<DependencySpec>() {

			@Override
			protected List<DependencySpec> getData() {
				return dependencies;
			}

		};
		
		add(new DataTable<DependencySpec, Void>("dependencies", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this));
			}
			
		});
	}

	private int getDependencyIndex(String job) {
		for (int i=0; i<dependencies.size(); i++) {
			if (dependencies.get(i).getJob().equals(job))
				return i;
		}
		return -1;
	}
	
	private abstract class ColumnFragment extends Fragment {

		private final int index;
		
		public ColumnFragment(String id, IModel<DependencySpec> model) {
			super(id, "columnFrag", DependencyListViewPanel.this, model);
			this.index = getDependencyIndex(getDependency().getJob());
			Preconditions.checkState(this.index != -1);
		}
		
		private DependencySpec getDependency() {
			return (DependencySpec) getDefaultModelObject();
		}

		protected abstract Component newLabel(String componentId);
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new SideFloating(target, SideFloating.Placement.RIGHT) {

						@Override
						protected String getTitle() {
							return getDependency().getJob();
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "dependency def-detail"));
						}

						@Override
						protected Component newBody(String id) {
							return BeanContext.viewBean(id, getDependency(), Sets.newHashSet("job"), true);
						}

					};
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
		
	}
}
