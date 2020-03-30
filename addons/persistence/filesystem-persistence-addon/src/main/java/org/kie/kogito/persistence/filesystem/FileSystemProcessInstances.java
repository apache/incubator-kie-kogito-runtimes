package org.kie.kogito.persistence.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kie.kogito.process.MutableProcessInstances;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceDuplicatedException;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.process.impl.marshalling.ProcessInstanceMarshaller;

@SuppressWarnings({"rawtypes"})
public class FileSystemProcessInstances implements MutableProcessInstances {

	private Process<?> process;
	private Path storage;

	private ProcessInstanceMarshaller marshaller;

	public FileSystemProcessInstances(Process<?> process, Path storage) {
		this(process, storage, new ProcessInstanceMarshaller());
	}

	public FileSystemProcessInstances(Process<?> process, Path storage, ProcessInstanceMarshaller marshaller) {
		this.process = process;
		this.storage = Paths.get(storage.toString(), process.id());
		this.marshaller = marshaller;

		try {
			Files.createDirectories(this.storage);
		} catch (IOException e) {
			throw new RuntimeException("Unable to create directories for file based storage of process instances", e);
		}
	}

	@Override
	public Optional findById(String id) {
		String resolvedId = resolveId(id);
		Path processInstanceStorage = Paths.get(storage.toString(), resolvedId);

        if (Files.notExists(processInstanceStorage)) {
            return Optional.empty();
        }
    	return (Optional<? extends ProcessInstance>) Optional.of(marshaller.unmarshallProcessInstance(readBytesFromFile(processInstanceStorage), process));

	}

	@Override
	public Collection values() {
		try {
			return Files.walk(storage)
					.filter(file -> !Files.isDirectory(file))
					.map(f -> marshaller.unmarshallProcessInstance(readBytesFromFile(f), process))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("Unable to read process instances ", e);
		}
	}

	@Override
	public boolean exists(String id) {
		return Files.exists(Paths.get(storage.toString(), resolveId(id)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void create(String id, ProcessInstance instance) {
		if (isActive(instance)) {
			String resolvedId = resolveId(id);
			Path processInstanceStorage = Paths.get(storage.toString(), resolvedId);

			if (Files.exists(processInstanceStorage)) {
				throw new ProcessInstanceDuplicatedException(id);
			}
			try {
				byte[] data = marshaller.marhsallProcessInstance(instance);
				Files.write(processInstanceStorage, data, StandardOpenOption.CREATE_NEW);

				disconnect(processInstanceStorage, instance);
			} catch (IOException e) {
				throw new RuntimeException("Unable to store process instance with id " + id, e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(String id, ProcessInstance instance) {
		if (isActive(instance)) {
			String resolvedId = resolveId(id);
			Path processInstanceStorage = Paths.get(storage.toString(), resolvedId);

			if (Files.exists(processInstanceStorage)) {
				try {
					byte[] data = marshaller.marhsallProcessInstance(instance);
					Files.write(processInstanceStorage, data, StandardOpenOption.TRUNCATE_EXISTING);

					disconnect(processInstanceStorage, instance);
				} catch (IOException e) {
					throw new RuntimeException("Unable to update process instance with id " + id, e);
				}
			}
		}
	}

	@Override
	public void remove(String id) {
		Path processInstanceStorage = Paths.get(storage.toString(), resolveId(id));

		try {
			Files.deleteIfExists(processInstanceStorage);
		} catch (IOException e) {
			throw new RuntimeException("Unable to remove process instance with id " + id, e);
		}

	}

	protected byte[] readBytesFromFile(Path processInstanceStorage) {
		try {
			return Files.readAllBytes(processInstanceStorage);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read process instance from " + processInstanceStorage, e);
		}
	}

	protected void disconnect(Path processInstanceStorage, ProcessInstance instance) {
		((AbstractProcessInstance<?>) instance).internalRemoveProcessInstance(() -> {

			try {
				byte[] reloaded = readBytesFromFile(processInstanceStorage);

	            return ((AbstractProcessInstance<?>)marshaller.unmarshallProcessInstance(reloaded, process, (AbstractProcessInstance<?>) instance)).internalGetProcessInstance();
			} catch (RuntimeException e) {
				return null;
			}


        });
	}

}
