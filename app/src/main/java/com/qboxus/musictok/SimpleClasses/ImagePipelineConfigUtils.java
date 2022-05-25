package com.qboxus.musictok.SimpleClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.disk.NoOpDiskTrimmableRegistry;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.NoOpMemoryTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;

import java.io.File;
import java.util.function.Supplier;

public class ImagePipelineConfigUtils {
    //Allocated free memory
    private static final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();
    //Maximum value of small pictures with very low disk space cache (Feature: a large number of small pictures can be placed on another disk space to prevent large pictures from occupying disk space and deleting a large number of small pictures)
    private static final int MAX_SMALL_DISK_VERYLOW_CACHE_SIZE = 20 * ByteConstants.MB;
    //Maximum value of low disk space cache for small pictures (Feature: a large number of small pictures can be placed on another disk space to prevent large pictures from occupying disk space and deleting a large number of small pictures)
    private static final int MAX_SMALL_DISK_LOW_CACHE_SIZE = 60 * ByteConstants.MB;
    //The maximum value of the default picture very low disk space cache
    private static final int MAX_DISK_CACHE_VERYLOW_SIZE = 20 * ByteConstants.MB;
    //The maximum value of the default graph low disk space cache
    private static final int MAX_DISK_CACHE_LOW_SIZE = 60 * ByteConstants.MB;
    //The maximum value of the disk cache of the default graph
    private static final int MAX_DISK_CACHE_SIZE = 100 * ByteConstants.MB;
    //The folder name of the path where the small picture is placed
    private static final String IMAGE_PIPELINE_SMALL_CACHE_DIR = "images_small_fresco";
    //The folder name of the path where the default image is placed
    private static final String IMAGE_PIPELINE_CACHE_DIR = "images_fresco";
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ImagePipelineConfig getDefaultImagePipelineConfig(Context context) {
        //Memory configuration

        DiskCacheConfig diskSmallCacheConfig = DiskCacheConfig.newBuilder(context).setBaseDirectoryPath(new File(Functions.getAppFolder(context)))//Cache image base path
                .setBaseDirectoryName(IMAGE_PIPELINE_SMALL_CACHE_DIR)//folder name
                .setMaxCacheSize(MAX_DISK_CACHE_SIZE)//The maximum size of the default cache.
                .setMaxCacheSizeOnLowDiskSpace(MAX_SMALL_DISK_LOW_CACHE_SIZE)//The maximum size of the cache, low disk space when using the device.
                .setMaxCacheSizeOnVeryLowDiskSpace(MAX_SMALL_DISK_VERYLOW_CACHE_SIZE)//The maximum size of the cache, when the device is extremely low disk space
                .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
                .build();
        //Disk configuration of the default picture
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context).setBaseDirectoryPath(new File(Functions.getAppFolder(context)))//Cache image base path
                .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)//folder name
                .setMaxCacheSize(MAX_DISK_CACHE_SIZE)//The maximum size of the default cache.
                .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)//The maximum size of the cache, low disk space when using the device.
                .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE)//The maximum size of the cache, when the device is extremely low disk space
                .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
                .build();
        //Cache image configuration
        ImagePipelineConfig.Builder configBuilder = ImagePipelineConfig.newBuilder(context)
                .setSmallImageDiskCacheConfig(diskSmallCacheConfig)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setMemoryTrimmableRegistry(NoOpMemoryTrimmableRegistry.getInstance())
                .setResizeAndRotateEnabledForNetwork(true);
        // is this piece of code, used to clean up the cache
        NoOpMemoryTrimmableRegistry.getInstance().registerMemoryTrimmable(new MemoryTrimmable() {
            @Override
            public void trim(MemoryTrimType trimType) {
                final double suggestedTrimRatio = trimType.getSuggestedTrimRatio();
                if (MemoryTrimType.OnCloseToDalvikHeapLimit.getSuggestedTrimRatio() == suggestedTrimRatio
                        || MemoryTrimType.OnSystemLowMemoryWhileAppInBackground.getSuggestedTrimRatio() == suggestedTrimRatio
                        || MemoryTrimType.OnSystemLowMemoryWhileAppInForeground.getSuggestedTrimRatio() == suggestedTrimRatio
                ) {
                    ImagePipelineFactory.getInstance().getImagePipeline().clearMemoryCaches();
                }
            }
        });
        return configBuilder.build();
    }
}
