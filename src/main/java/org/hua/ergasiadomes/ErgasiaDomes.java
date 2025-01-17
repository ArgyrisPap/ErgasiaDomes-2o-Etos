
package org.hua.ergasiadomes;
import java.util.Random;
public class ErgasiaDomes {

    public static void main(String[] args) {
        Random random=new Random();
        int capacity=100,num;
        CacheReplacementPolicy lru=CacheReplacementPolicy.LRU;
        CacheReplacementPolicy mru=CacheReplacementPolicy.MRU;
        CacheReplacementPolicy lfu=CacheReplacementPolicy.LFU;
        MyCache <Integer,String> cache=new MyCache<>(capacity,lru);
        MyCache <Integer,String> cache2=new MyCache<>(capacity,mru);
        MyCache <Integer,String> cache3=new MyCache<>(capacity,lfu);
        
        while(cache.getTotalOperations()<100000){
            if(random.nextDouble()<0.8){
                //80% πιθανοτητα
                //απο το  0-49
                num=random.nextInt(50);
            }else {
                //απο το 50-149
                num=50+random.nextInt(150);
            }
            //50% να κανει put/get
            if (random.nextBoolean()){
                cache.put(num, "Value is "+num);
                cache2.put(num, "Value is "+num);
                cache3.put(num, "Value is "+num);
            }else{
                cache.get(num);
                cache2.get(num);
                cache3.get(num);
            }
        }
        
        System.out.println(lru.getDescription());
        cache.printTotalOperations();
        System.out.println("==================\n"+mru.getDescription());
        cache2.printTotalOperations();
        System.out.println("==================\n"+lfu.getDescription());
        cache3.printTotalOperations();
        
    }
}
