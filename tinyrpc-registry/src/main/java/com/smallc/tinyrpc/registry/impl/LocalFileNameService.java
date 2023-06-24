package com.smallc.tinyrpc.registry.impl;

import com.smallc.tinyrpc.registry.Metadata;
import com.smallc.tinyrpc.registry.NameService;
import com.smallc.tinyrpc.common.serializer.SerializeType;
import com.smallc.tinyrpc.common.serializer.SerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.registry.impl
 */
public class LocalFileNameService implements NameService {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileNameService.class);
    private static final Collection<String> schemes = Collections.singleton("file");
    private File file;

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

                    metadata = SerializeUtil.deserialize(bytes, Metadata.class, SerializeType.HESSIAN.getMessage());
                } else {
                    metadata = new Metadata();
                }
                List<URI> uris = metadata.computeIfAbsent(serviceName, k -> new ArrayList<>());
                if (!uris.contains(uri)) {
                    uris.add(uri);
                }
                logger.info(metadata.toString());

                bytes = SerializeUtil.serialize(metadata, SerializeType.HESSIAN.getMessage());
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
                metadata = bytes.length == 0 ? new Metadata() : SerializeUtil.deserialize(bytes, Metadata.class, SerializeType.HESSIAN.getMessage());
                logger.info(metadata.toString());
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

}
