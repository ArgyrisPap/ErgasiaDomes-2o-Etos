package org.hua.ergasiadomes;

public enum CacheReplacementPolicy {
    LRU("Least recent used"),
    MRU("Most recent used"),
    LFU("Least Frequently Used");
    
    private final String description;
    
    CacheReplacementPolicy(String description){
        this.description=description;
    }
    public String getDescription(){return description;}
}
