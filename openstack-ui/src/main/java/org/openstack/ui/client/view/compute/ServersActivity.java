package org.openstack.ui.client.view.compute;

import java.util.Collection;
import java.util.Set;

import org.openstack.model.compute.Server;
import org.openstack.model.compute.ServerList;
import org.openstack.model.compute.nova.NovaServerForCreate;
import org.openstack.model.compute.nova.server.actions.Console;
import org.openstack.model.compute.nova.server.actions.GetConsoleOutputAction;
import org.openstack.model.compute.nova.server.actions.GetVncConsoleAction;
import org.openstack.ui.client.OpenStackPlace;
import org.openstack.ui.client.api.DefaultAsyncCallback;
import org.openstack.ui.client.api.OpenStackClient;
import org.openstack.ui.client.api.RefreshableDataProvider;
import org.openstack.ui.client.view.compute.widgets.ServerAction;
import org.openstack.ui.client.view.compute.wizards.CreateServerActivity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;

public class ServersActivity extends AbstractActivity implements ServersView.Presenter {
	
	private static final ServersView VIEW = new ServersView();
	
	private OpenStackPlace place;

	public ServersActivity(OpenStackPlace place) {
		this.place = place;
	}

	private RefreshableDataProvider<Server> dataProvider;

	private MultiSelectionModel<Server> selectionModel = new MultiSelectionModel<Server>();

	private DefaultSelectionEventManager<Server> selectionManager = DefaultSelectionEventManager
			.<Server> createCheckboxManager(0);

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		GWT.log("Start Server Activity for " + OpenStackClient.getTenant());
		VIEW.setPresenter(this);
		panel.setWidget(VIEW);
		VIEW.grid.setSelectionModel(selectionModel, selectionManager);
		dataProvider = new RefreshableDataProvider<Server>(VIEW.grid) {

			@Override
			protected void onRangeChanged(HasData<Server> display) {
				OpenStackClient.COMPUTE.listServers(new DefaultAsyncCallback<ServerList>() {

					@Override
					public void onSuccess(ServerList result) {
						updateRowCount(result.getList().size(), true);
						updateRowData(0, result.getList());

					}
				});
			}

		};

	}

	@Override
	public void onCreateServer() {
		NovaServerForCreate serverForCreate = new NovaServerForCreate();
		CreateServerActivity createServerActivity = new CreateServerActivity(serverForCreate);
		createServerActivity.onStart();
	}

	@Override
	public void refresh() {
		dataProvider.refresh();
	}

	@Override
	public void onServerAction(final ServerAction action) {
		final Set<Server> servers = selectionModel.getSelectedSet();
		switch (action) {
		case GET_CONSOLE_OUTPUT:
			showConsoleOutput(servers);
			break;
		case GET_VNC_CONSOLE:
			showVncConsole(servers);
			break;
		default:
			action.execute(servers, new DefaultAsyncCallback<Void>() {

				@Override
				public void onSuccess(Void result) {
					Window.alert(action.toString() + " executed successfully on " + servers.size() + " servers");

				}
			});
			break;
		}
	}

	public void showVncConsole(Collection<Server> servers) {
		for (Server server : servers) {

			final PopupPanel popup = new PopupPanel(true, true);

			OpenStackClient.COMPUTE.getVncConsole(server.getId(), new GetVncConsoleAction(), new AsyncCallback<Console>() {

				@Override
				public void onSuccess(Console console) {
					Frame frame = new Frame(console.getUrl());
					frame.setHeight("430px");
					frame.setWidth("720px");
					popup.setWidget(frame);
					popup.center();
					frame.getElement().focus();
				}

				@Override
				public void onFailure(Throwable caught) {

				}

			});
			break;
		}
	}

	public void showConsoleOutput(Collection<Server> servers) {
		for (Server server : servers) {

			final PopupPanel popup = new PopupPanel(true, true);

			OpenStackClient.COMPUTE.getConsoleOutput(server.getId(), new GetConsoleOutputAction(), new DefaultAsyncCallback<String>() {

				@Override
				public void onSuccess(String result) {
					ScrollPanel scroll = new ScrollPanel();
					scroll.setWidth("980px");
					scroll.setHeight("400px");
					SafeHtmlBuilder builder = new SafeHtmlBuilder();
					builder.appendHtmlConstant("<code>").appendEscapedLines(result).appendHtmlConstant("<code>");
					HTML html = new HTML(builder.toSafeHtml());
					scroll.add(html);
					popup.setWidget(scroll);
					popup.center();
					scroll.scrollToBottom();
				}

			});
			break;
		}
	}

	@Override
	public void onDeleteServers() {
		for(Server ns : selectionModel.getSelectedSet()) {
			OpenStackClient.COMPUTE.deleteServer(ns.getId(), new DefaultAsyncCallback<Void>() {

				@Override
				public void onSuccess(Void result) {
					refresh();
				}
			});
		}
	}

	@Override
	public void onShowServer(String id) {
		OpenStackClient.COMPUTE.showServer(id, new DefaultAsyncCallback<Server>() {

			@Override
			public void onSuccess(Server result) {
				VIEW.detail.setWidget(new Label(result.toString()));
				
			}
		});
		
	}

	@Override
	public boolean isSelected(Server object) {
		return selectionModel.isSelected(object);
	}

}
