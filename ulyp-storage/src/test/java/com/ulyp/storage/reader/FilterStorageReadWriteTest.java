package com.ulyp.storage.reader;

import com.ulyp.core.*;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.mem.SerializedTypeList;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.util.StubRecordingDataReaderJob;
import com.ulyp.storage.util.TestMemPageAllocator;
import com.ulyp.storage.writer.FileRecordingDataWriter;
import com.ulyp.storage.writer.RecordingDataWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterStorageReadWriteTest {

    private final T callee = new T();
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(T.class);
    private final Method method = Method.builder()
            .type(type)
            .name("run")
            .id(1000)
            .constructor(false)
            .isStatic(false)
            .returnsSomething(true)
            .build();
    private final SerializedTypeList types = new SerializedTypeList();
    private final SerializedMethodList methods = new SerializedMethodList();
    private RecordingDataReader reader;
    private RecordingDataWriter writer;
    private File file;

    public static class T {
        public String foo(String in) {
            return in;
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        file = Files.createTempFile(FilterStorageReadWriteTest.class.getSimpleName(), "a").toFile();
        writer = new FileRecordingDataWriter(file);
        types.add(type);
        methods.add(method);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        reader.close();
        writer.close();
    }

    @Test
    void testReaderJobSubmit() throws ExecutionException, InterruptedException, TimeoutException {
        this.reader = new FileRecordingDataReaderBuilder(file)
            .build();

        SerializedRecordedMethodCallList methodCalls1 = new SerializedRecordedMethodCallList(1, new TestMemPageAllocator());

        methodCalls1.addEnterMethodCall(method.getId(), typeResolver, callee, new Object[]{"ABC"});
        methodCalls1.addEnterMethodCall(method.getId(), typeResolver, callee, new Object[]{"ABC"});
        methodCalls1.addEnterMethodCall(method.getId(), typeResolver, callee, new Object[]{"ABC"});
        methodCalls1.addEnterMethodCall(method.getId(), typeResolver, callee, new Object[]{"ABC"});

        SerializedRecordedMethodCallList methodCalls2 = new SerializedRecordedMethodCallList(1, new TestMemPageAllocator());
        methodCalls2.addExitMethodCall(3, typeResolver, "BVBC");
        methodCalls2.addExitMethodCall(2, typeResolver, "XCZXC");

        writer.write(types);
        writer.write(methods);

        writer.write(RecordingMetadata.builder().id(1).build());
        writer.write(methodCalls1);
        writer.write(methodCalls2);
        writer.close();

        StubRecordingDataReaderJob job = new StubRecordingDataReaderJob();
        CompletableFuture<Void> voidCompletableFuture = reader.submitReaderJob(job);
        voidCompletableFuture.get(1, TimeUnit.MINUTES);

        assertEquals(1, job.getRecordingMetadatas().size());

        Type typeDeserialized = job.getTypes().get(type.getId());
        assertEquals(typeDeserialized, type);

        assertEquals(1, job.getMethods().size());
        Method methodDeserialized = job.getMethods().get(method.getId());
        assertEquals(methodDeserialized, method);

        Map<Integer, List<MethodCall>> recordedCalls = job.getRecordedCalls();
        List<MethodCall> calls = recordedCalls.get(1);

        assertEquals(6, calls.size());
    }
}
