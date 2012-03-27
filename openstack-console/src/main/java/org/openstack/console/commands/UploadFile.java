package org.openstack.console.commands;

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.openstack.api.storage.AccountResource;
import org.openstack.api.storage.ContainerResource;
import org.openstack.console.model.StoragePath;
import org.openstack.model.storage.swift.SwiftStorageObjectProperties;

public class UploadFile extends OpenstackCliCommandRunnerBase {
	@Argument(index = 0)
	public StoragePath path;

	@Argument(index = 1, multiValued = true)
	public List<String> properties;

	public UploadFile() {
		super("upload", "file");
	}

	@Override
	public Object runCommand() throws Exception {
		AccountResource client = getStorageClient();

		String[] tokens = path.getKey().split("/");
		if (tokens.length != 2) {
			throw new IllegalArgumentException("Cannot parse: " + path.getKey());
		}
		ContainerResource container = client.container(tokens[0]);

		SwiftStorageObjectProperties objectProperties = new SwiftStorageObjectProperties();
		objectProperties.setName(tokens[1]);

		if (properties != null) {
			for (String property : properties) {
				int equalsIndex = property.indexOf('=');
				if (equalsIndex == -1) {
					throw new IllegalArgumentException("Can't parse: " + property);
				}

				String key = property.substring(0, equalsIndex);
				String value = property.substring(equalsIndex + 1);

				objectProperties.getCustomProperties().put(key, value);
			}
		}

		// This command will probably be faster _not_ in nailgun mode
		// InputStream stream = new NoCloseInputStream(System.in);
		container.object(tokens[1]).put(System.in, -1, objectProperties);

		return path.getKey();
	}
}
