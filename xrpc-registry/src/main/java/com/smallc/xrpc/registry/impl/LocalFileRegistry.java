package com.smallc.xrpc.registry.impl;

import com.smallc.xrpc.registry.Registry;
import com.smallc.xrpc.common.serializer.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.registry.impl
 */
public class LocalFileRegistry implements Registry {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileRegistry.class);
    private static final Collection<String> schemes = Collections.singleton("file");
    private File file;

    /**
     * Map
     * String    服务名
     * List<URI> 服务提供者URI列表
     */
    class Metadata extends HashMap<String, List<URI>> {

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Metadata:").append("\n");
            for (Entry<String, List<URI>> entry : entrySet()) {
                sb.append("\t").append("Classname: ")
                        .append(entry.getKey()).append("\n");
                sb.append("\t").append("URIs:").append("\n");
                for (URI uri : entry.getValue()) {
                    sb.append("\t\t").append(uri).append("\n");
                }
            }
            return sb.toString();
        }

    }

    @Override
    public Collection<String> supportedSchemes() {
        return schemes;
    }

    @Override
    public void connect(URI nameServiceUri) {
        if (schemes.contains(nameServiceUri.getScheme())) {
            file = new File(nameServiceUri);
        } else {
            throw new RuntimeException("Unsupported scheme!");
        }
    }

    @Override
    public synchronized void registerService(String serviceName, URI uri) throws IOException {
        logger.info("Register service: {}, uri: {}.", serviceName, uri);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel fileChannel = raf.getChannel()) {
            FileLock lock = fileChannel.lock();
            try {
                int fileLength = (int) raf.length();
                Metadata metadata;
                byte[] bytes;
                if (fileLength > 0) {
                    bytes = new byte[(int) raf.length()];
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    while (buffer.hasRemaining()) {
                        fileChannel.read(buffer);
                    }

                    metadata = SerializationUtil.deserialize(bytes, Metadata.class);
                } else {
                    metadata = new Metadata();
                }
                List<URI> uris = metadata.computeIfAbsent(serviceName, k -> new ArrayList<>());
                if (!uris.contains(uri)) {
                    uris.add(uri);
                }
                logger.debug(metadata.toString());

                bytes = SerializationUtil.serialize(metadata);
                fileChannel.truncate(bytes.length);
                fileChannel.position(0L);
                fileChannel.write(ByteBuffer.wrap(bytes));
                fileChannel.force(true);
            } finally {
                lock.release();
            }
        }
    }

    @Override
    public List<URI> getServiceAddress(String serviceName) throws IOException {
        Metadata metadata;
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel fileChannel = raf.getChannel()) {
            FileLock lock = fileChannel.lock();
            try {
                byte[] bytes = new byte[(int) raf.length()];
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    fileChannel.read(buffer);
                }
                metadata = (bytes.length == 0) ? new Metadata() : SerializationUtil.deserialize(bytes, Metadata.class);
                logger.debug(metadata.toString());
            } finally {
                lock.release();
            }
        }

        List<URI> uris = metadata.get(serviceName);
        if (null == uris || uris.isEmpty()) {
            return null;
        } else {
            return uris;
        }
    }

    @Override
    public Map<String, List<URI>> broadcastServiceAddress() {
        return null;
    }

}
