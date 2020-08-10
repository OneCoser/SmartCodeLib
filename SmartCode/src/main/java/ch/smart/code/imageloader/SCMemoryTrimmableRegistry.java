package ch.smart.code.imageloader;

import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.MemoryTrimmableRegistry;

import java.util.concurrent.CopyOnWriteArrayList;

public class SCMemoryTrimmableRegistry implements MemoryTrimmableRegistry {
    
    private CopyOnWriteArrayList<MemoryTrimmable> mList = new CopyOnWriteArrayList<>();
    
    
    @Override
    public void unregisterMemoryTrimmable(MemoryTrimmable trimmable) {
        mList.remove(trimmable);
    }
    
    
    @Override
    public void registerMemoryTrimmable(MemoryTrimmable trimmable) {
        mList.add(trimmable);
    }
    
    
    public void onTrimMemory(MemoryTrimType trimType) {
        if (mList == null || mList.isEmpty()) {
            return;
        }
        for (MemoryTrimmable trimmable : mList) {
            trimmable.trim(trimType);
        }
    }
}
