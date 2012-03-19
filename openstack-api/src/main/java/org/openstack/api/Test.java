package org.openstack.api;

import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

import org.openstack.api.common.RestClient;
import org.openstack.api.compute.TenantResource;
import org.openstack.api.identity.IdentityResource;
import org.openstack.api.imagestore.ImageResource;
import org.openstack.api.storage.AccountResource;
import org.openstack.model.common.OpenStackSession2;
import org.openstack.model.compute.NovaFlavorList;
import org.openstack.model.compute.NovaImageList;
import org.openstack.model.compute.NovaKeyPairList;
import org.openstack.model.compute.NovaSecurityGroupList;
import org.openstack.model.compute.NovaServerList;
import org.openstack.model.compute.NovaVolumeList;
import org.openstack.model.identity.KeyStoneAccess;
import org.openstack.model.identity.KeyStoneAuthentication;
import org.openstack.model.identity.KeyStoneRole;
import org.openstack.model.identity.KeyStoneRoleList;
import org.openstack.model.identity.KeyStoneService;
import org.openstack.model.identity.KeyStoneServiceList;
import org.openstack.model.identity.KeyStoneTenant;
import org.openstack.model.identity.KeyStoneTenantList;
import org.openstack.model.identity.KeyStoneUser;
import org.openstack.model.identity.KeyStoneUserList;

public class Test {

	public static void main(String[] args) {
		Client client = RestClient.INSTANCE.verbose(true).getJerseyClient();
		KeyStoneAuthentication authentication = new KeyStoneAuthentication().withPasswordCredentials("admin", "secret0");
		IdentityResource identity = IdentityResource.endpoint(client,"http://192.168.1.52:35357/v2.0");
		KeyStoneAccess access = identity.tokens().authenticate(authentication);
		access.getToken().setId("secret0");
		OpenStackSession2 session = new OpenStackSession2();
		session.setAccess(access);
		identity.setSession(session);

		KeyStoneTenantList tenants = identity.tenants().get(new HashMap<String, Object>());

		KeyStoneTenant tenant = new KeyStoneTenant();
		tenant.setName("test");
		tenant.setDescription("desc");
		tenant.setEnabled(true);
		tenant = identity.tenants().post(Entity.json(tenant));

		tenant = identity.tenants().tenant(tenant.getId()).get();

		identity.tenants().tenant(tenant.getId()).delete();

		KeyStoneUserList users = identity.users().get(new HashMap<String, Object>());

		KeyStoneUser user = new KeyStoneUser();
		user.setName("test");
		user.setPassword("secret0");
		user.setEmail("test@test.com");
		user.setEnabled(true);
		user = identity.users().post(Entity.json(user));

		user = identity.users().user(user.getId()).get();

		identity.users().user(user.getId()).delete();

		KeyStoneRoleList roles = identity.roles().get(new HashMap<String, Object>());

		KeyStoneRole role = new KeyStoneRole();
		role.setName("test");
		role = identity.roles().post(Entity.json(role));

		role = identity.roles().role(role.getId()).get();

		identity.roles().role(role.getId()).delete();

		KeyStoneServiceList services = identity.services().get(new HashMap<String, Object>());

		KeyStoneService service = new KeyStoneService();
		service.setName("test");
		service.setType("compute");
		service.setDescription("Nova 3");
		service = identity.services().post(Entity.json(service));

		service = identity.services().service(service.getId()).get();

		identity.services().service(service.getId()).delete();

//		KeyStoneEndpointTemplatesList endpointTemplates = identity.endpoints().get(new HashMap<String, Object>() {{
//			put("Accept", MediaType.APPLICATION_XML);
//		}});
		
		authentication = new KeyStoneAuthentication().withPasswordCredentials("admin", "secret0");
		identity = IdentityResource.endpoint(client,"http://192.168.1.52:5000/v2.0");
		access = identity.tokens().authenticate(authentication);
		session.setAccess(access);
		identity.setSession(session);
		
		tenants = identity.tenants().get(new HashMap<String, Object>());
		
		authentication = new KeyStoneAuthentication().withTokenAndTenant(access.getToken().getId(), tenants.getList().get(0).getId());
		access = identity.tokens().authenticate(authentication);
		session.setAccess(access);
		identity.setSession(session);

		TenantResource compute = TenantResource.endpoint(client, "http://192.168.1.52:8774/v2/" + tenants.getList().get(0).getId());
		compute.setSession(session);
		NovaServerList servers = compute.servers().get(new HashMap<String, Object>(){{
			put("detail",true);
		}});
		
		NovaImageList images = compute.images().get(new HashMap<String,Object>());
		// "cirros-0.3.0-x86_64-blank"
		
		NovaFlavorList flavors = compute.flavors().get(new HashMap<String, Object>());
		
		NovaKeyPairList keypairs = compute.keyPairs().get(new HashMap<String, Object>());
		
		NovaSecurityGroupList securityGroups = compute.securityGroups().get(new HashMap<String, Object>());
		
		NovaVolumeList volumes = compute.volumes().get(new HashMap<String, Object>());

		// NovaSnapshotList snapshots = compute.snapshots().get(new
		// HashMap<String, Object>(){{
		// put("X-Auth-Token", access2.getToken().getId());
		// }});

		ImageResource image = ImageResource.endpoint(client, "http://192.168.1.52:9292/v1");
		image.setSession(session);

		AccountResource storage = AccountResource.endpoint(client, "http://192.168.1.52:3333");
		storage.setSession(session);

		// IdentityResource identity = IdentityResource.endpoint(client,
		// "http://192.168.1.52:35357/v2.0");

		/*
		 * OpenStackSession session =
		 * OpenStackSession.create().with(Feature.VERBOSE); String authUrl =
		 * "http://192.168.1.45:5000/v2.0"; OpenstackCredentials credentials =
		 * new OpenstackCredentials(authUrl, "demo", "secret0", "demo");
		 * session.authenticate(credentials);
		 * 
		 * // X-Auth-Token has been set on session object
		 * 
		 * IdentityResource identity = session.getAuthenticationClient().root();
		 * 
		 * KeyStoneTenantList tenants = identity.tenants().list(); for
		 * (KeyStoneTenant tenant : tenants.getList()) {
		 * System.out.println(tenant); }
		 * 
		 * // I will choose the first tenant for (KeyStoneTenant tenant :
		 * tenants.getList()) { OpenstackCredentials tenantCredentials =
		 * credentials.withTenant(tenant.getName());
		 * session.authenticate(tenantCredentials); break; }
		 * 
		 * TenantResource compute = session.getComputeClient().root(); for
		 * (NovaServer s : compute.servers().list().getList()) {
		 * System.out.println(s); }
		 * 
		 * Iterable<NovaImage> images = compute.images().list().getList();
		 * NovaImage image = null; for (NovaImage i : images) {
		 * System.out.println(i); if
		 * (i.getName().equals("cirros-0.3.0-x86_64-blank")) { image = i; break;
		 * } }
		 * 
		 * System.out.println(image);
		 * 
		 * Iterable<NovaFlavor> flavors = compute.flavors().list().getList();
		 * for (NovaFlavor f : flavors) { System.out.println(f); }
		 * 
		 * Iterable<NovaServer> servers = compute.servers().list().getList();
		 * for (NovaServer s : servers) { ServerResource sr = new
		 * ServerResource(session, s); System.out.println(sr.get(true).show());
		 * }
		 */
		// ServerForCreate serverForCreate = new ServerForCreate();
		// serverForCreate.setName("eureka1");
		// serverForCreate.setFlavorRef("1");
		// serverForCreate.setImageRef(image.getId());
		// serverForCreate.setSecurityGroups(new
		// ArrayList<ServerForCreate.SecurityGroup>() {{
		// add(new ServerForCreate.SecurityGroup("test"));
		// }});
		// System.out.println(serverForCreate);

		/*
		 * AsyncServerOperation async = nova.createServer(serverForCreate);
		 * Server server = async.get();
		 */

		// Server server = compute.servers().create(serverForCreate);
		// System.out.println(server.getImage(session).getMinDisk());
		//
		// System.out.println(server);
		// System.out.println("DELETING");
		// compute.servers().server(server.getId()).delete();

		// TODO: Review the endpoint resolver for admin.
		// for (User user : identity.users().list()) {
		// System.out.println(user);
		// }

		/*
		 * RolesResource rolesResource = identity.roles(); RolesRepresentation
		 * rolesRepresentation = rolesResource.list(); List<Role> roles =
		 * rolesRepresentation.getList(); for(Role role : roles) {
		 * System.out.println(role); }
		 * 
		 * RoleResource roleResource = rolesResource.role(roles.get(0).getId());
		 * System.out.println(roleResource.show());
		 */

		/*
		 * ServicesResource servicesResource = identity.services();
		 * ServicesRepresentation servicesRepresentation =
		 * servicesResource.list(); List<Service> services =
		 * servicesRepresentation.getList(); for(Service service : services) {
		 * System.out.println(service); }
		 * 
		 * ServiceResource serviceResource =
		 * servicesResource.service(services.get(0).getId());
		 * System.out.println(serviceResource.show());
		 */

		/*
		 * EndpointTemplatesResource endpointTemplatesResource =
		 * identity.endpointTemplates(); EndpointTemplatesRepresentation
		 * endpointTemplatesRepresentation =
		 * identity.endpointTemplates().list(); List<EndpointTemplate>
		 * endpointTemplates = endpointTemplatesRepresentation.getList();
		 * for(EndpointTemplate endEndpointTemplate : endpointTemplates) {
		 * System.out.println(endEndpointTemplate); }
		 * 
		 * EndpointTemplateResource endpointTemplateResource =
		 * endpointTemplatesResource.endpointTemplate(services.get(0).getId());
		 * System.out.println(endpointTemplateResource.show());
		 */

		// representation = representation.next();
		// System.out.println(representation.getList());

		// ComputeResource compute = new ComputeResource(client,
		// "http://192.168.1.49:8774/v1.1");
		// TenantResource tenant = compute.tenant(); // tenants.get(0).getId());

		// client.resource("http://192.168.1.49:8774/v2/1/extensions").accept(MediaType.APPLICATION_JSON).get(String.class);

		/*
		 * FlavorsRepresentation flavorsRepresentation =
		 * tenant.flavors().list(); flavorsRepresentation.fetchAll(); for(Flavor
		 * flavor : flavorsRepresentation.getList()) {
		 * System.out.println(flavor); }
		 * 
		 * 
		 * ImagesRepresentation imagesRepresentation = tenant.images().list();
		 * imagesRepresentation.fetchAll(); for(Image image :
		 * imagesRepresentation.getList()) { System.out.println(image); }
		 * 
		 * 
		 * ServerForCreate serverForCreate = new ServerForCreate();
		 * serverForCreate.setName("eureka"); serverForCreate.setFlavorRef("1");
		 * serverForCreate
		 * .setImageRef(imagesRepresentation.getList().get(1).getId());
		 * ServerRepresentation serverRepresentation =
		 * tenant.servers().create(serverForCreate);
		 * System.out.println(serverRepresentation.getModel());
		 * 
		 * 
		 * 
		 * ServersRepresentation serversRepresentation =
		 * tenant.servers().list(true); //serversRepresentation.fetchAll();
		 * List<Server> servers = serversRepresentation.getList(); for(Server
		 * server : servers) { System.out.println(server); }
		 * 
		 * 
		 * 
		 * 
		 * System.out.println(tenant.servers().server(servers.get(0).getId()).
		 * getConsoleOutput(20));
		 * System.out.println(tenant.servers().server(servers
		 * .get(0).getId()).getVncConsole("novnc"));
		 */
		// System.out.println(tenant.zones().list().getList());

		// tenant.servers().server(servers.get(0).getId()).getVNCConsole("novnc");

		/*
		 * ConsolesResource consolesResource =
		 * tenant.servers().server(servers.get(0).getId()).consoles();
		 * consolesResource.create(); ConsoleList consoleList =
		 * consolesResource.list(); for(Console console : consoleList.getList())
		 * { System.out.println(console); }
		 */

		/*
		 * KeyPairsResource keyPairsResource =
		 * compute.tenant(tenants.get(0).getId()).keyPairs(); KeyPairList
		 * keyPairList = keyPairsResource.list(); for(KeyPair keyPair :
		 * keyPairList.getList()) { System.out.println(keyPair); }
		 */
		/*
		 * SecurityGroupsResource securityGroupsResource =
		 * tenant.securityGroups(); //SecurityGroup sg = new SecurityGroup();
		 * //sg.setName("test"); //sg.setDescription("desc"); //sg =
		 * securityGroupsResource.create(sg); SecurityGroupList
		 * securityGroupList = securityGroupsResource.list(); for(SecurityGroup
		 * securityGroup : securityGroupList.getList()) {
		 * System.out.println(securityGroup); }
		 */
		/*
		 * SecurityGroupRulesResource rulesResource =
		 * securityGroupsResource.securityGroup
		 * (securityGroupList.getList().get(0).getId()).rules();
		 * SecurityGroupRuleForCreate sgr = new SecurityGroupRuleForCreate();
		 * sgr.setIpProtocol("TCP");
		 * sgr.setParentGroupId(securityGroupList.getList().get(0).getId());
		 * //sgr.setGroupId(securityGroupList.getList().get(0).getId());
		 * sgr.setFromPort(22); sgr.setToPort(23); sgr.setCidr("0.0.0.0/0");
		 * //rulesResource.create(sgr);
		 * 
		 * //client.resource(
		 * "http://192.168.1.49:8774/v1.1/1/os-security-group-rules"
		 * ).accept(MediaType.APPLICATION_XML)
		 * .type(MediaType.APPLICATION_XML).post(SecurityGroupRule.class, sgr);
		 */
		/*
		 * VolumesResource volumesResource = tenant.volumes(); Volume vol = new
		 * Volume(); vol.setSizeInGB(1); vol.setType(""); VolumeList volumeList
		 * = volumesResource.list(false); for(Volume volumex :
		 * volumeList.getList()) { System.out.println(volumex); }
		 */

		/*
		 * ZonesResource zonesResource = tenant.zones(); Zone zone = new Zone();
		 * zone.setName("dos"); zonesResource.create(zone); ZoneList zoneList =
		 * zonesResource.detail(); for(Zone zonex : zoneList.getList()) {
		 * System.out.println(zonex); }
		 */

	}

}