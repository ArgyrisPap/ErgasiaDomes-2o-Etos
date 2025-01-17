package org.hua.ergasiadomes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class MyCacheTest {
    
    @Test
    public void testCacheGetAndPut() {
        //προσθετει στοιχεια στο cache
        MyCache<Integer, Integer> cache;
        for (CacheReplacementPolicy p: CacheReplacementPolicy.values()){
            cache=new MyCache(3,p);
            cache.put(1, 1);
            cache.put(2, 2);
        
            //επιστρεφει στοιχεια απο το cache
            assertEquals(1, cache.get(1));  //το cache πρεπει να επιστρεφει 1 για το key 1
            assertEquals(2, cache.get(2));  //το cache πρεπει να επιστρεφει 2 για το key 2
        }
        
    }
    
    @Test
    public void testCacheEviction() {
        MyCache<Integer, Integer> cache;

        for (CacheReplacementPolicy policy : CacheReplacementPolicy.values()) {
            
            cache = new MyCache<>(3, policy);
            cache.put(1, 1);
            cache.put(2, 2);
            cache.put(3, 3);
            switch (policy) {
                case LRU:
                    cache.put(4, 4);
                    assertNull(cache.get(1)); 
                    assertEquals(2,cache.get(2));
                    assertEquals(3,cache.get(3));
                    assertEquals(4,cache.get(4));
                    break;

                case MRU:
                    cache.put(4, 4);
                    assertEquals(1,cache.get(1));
                    assertEquals(2,cache.get(2));
                    assertNull(cache.get(3)); 
                    assertEquals(4,cache.get(4));
                    break;

                case LFU:
                    cache.get(3); // Συχνότητα του 3 = 2
                    cache.get(2); // Συχνότητα του 2 = 2

                    // Εισαγωγή 4 -> διωχνεται το  1 (συχνότητα 1)
                    cache.put(4, 4); 
                    assertNull(cache.get(1)); // Το 1 έχει βγει εκτος
                    assertEquals(2,cache.get(2)); // Το 2 παραμένει, νεα συχνοτητα (λογω του cache.get(2)=3)
                    assertEquals(3,cache.get(3)); // Το 3 παραμένει, νεα συχνοτητα (λογω του cache.get(3)=3)
                    cache.put(5, 5); //Εισαγωγή 5 -> διωχνεται το  4 (συχνότητα 1)
                    cache.put(6, 6); //Εισαγωγή 6 -> διωχνεται το  5 (συχνότητα 1)
                    assertNull(cache.get(4)); // Το 4 βγηκε εκτος
                    assertNull(cache.get(5));// Το 5 βγηκε εκτος
                    assertEquals(6,cache.get(6));
                    
                    break;
            }
        }
    
    } 

    @Test
    public void testCacheUpdateValue() {
        MyCache<Integer, Integer> cache;
        for (CacheReplacementPolicy p: CacheReplacementPolicy.values()){
            cache=new MyCache(3,p);
            cache.put(1, 1);  //{1=1}
            cache.put(2, 2);  //{1=1, 2=2}

            cache.put(1, 10); //{2=2,1=10} (κανει update το value του key 1)

            assertEquals(10, cache.get(1));  //αρα το value του key 1 πρεπει να ειναι 10
            assertEquals(2, cache.get(2));   //το key 2 πρεπει να παραμενει 2
        }
    }

    @Test
    public void testCacheAccessOrder() {
        
        MyCache<Integer, Integer> cache;
        for (CacheReplacementPolicy p: CacheReplacementPolicy.values()){
            cache=new MyCache(3,p);
        
            cache.put(1, 1);  //{1=1}
            cache.put(2, 2);  //{1=1, 2=2}
            cache.put(3, 3);  //{1=1, 2=2, 3=3}

            //ετσι το key 2 γινεται το πιο προσφατα χρησιμοποιημενο
            cache.get(2); // {1=1, 3=3, 2=2}

            
            cache.put(4, 4);  
            if (p==CacheReplacementPolicy.LRU){
                //προσθαιτοντας καινουργιο key πρεπει να αφαιρει το λιγοτερο προσφατα χρησιμοποιημενο key, δηλαδη το key 1
                //{3=3, 2=2, 4=4}
                assertEquals(null, cache.get(1));  //key 1 πρεπει να εχει αφαιρεθει
                assertEquals(3, cache.get(3));  //key 3 πρεπει να υπαρχει
                assertEquals(2, cache.get(2));  //key 2 πρεπει να υπαρχει
                assertEquals(4, cache.get(4));  //key 4 πρεπει να υπαρχει
            }else if (p==CacheReplacementPolicy.MRU){
                //προσθαιτοντας καινουργιο key πρεπει να αφαιρει το πιο προσφατα χρησιμοποιημενο key, δηλαδη το key 2
                //{1=1, 3=3, 4=4}
                assertEquals(null, cache.get(2));  //key 2 πρεπει να εχει αφαιρεθει
                assertEquals(3, cache.get(3));  //key 3 πρεπει να υπαρχει
                assertEquals(1, cache.get(1));  //key 1 πρεπει να υπαρχει
                assertEquals(4, cache.get(4));  //key 4 πρεπει να υπαρχει
            }else if(p==CacheReplacementPolicy.LFU) {
                //Υπο κανονικες συνθηκες δεν μπορουμε να πουμε με σιγουρια ποιο απο τα δυο (το 1ο η 3ο) αφαιρεθηκε
                //εξαρταται πιο εχει μικροτερο 'hashcode (βαση δικη μασ υλοποιησης)
                //Στην συγκεκριμενη περιπτωση ξερουμε πως αφαιρεσε το node που εχει το χαμηλοτερο (αριθμητικα) κλειδι
                // γιατι το hashcode ενος Int ειναι ο ιδιος ο int, αρα αφαιρεθηκε ο node(1,1)
                    assertEquals(null, cache.get(1));
                    assertEquals(3, cache.get(3));  //key 3 πρεπει να υπαρχει
                    assertEquals(2, cache.get(2));  //key 1 πρεπει να υπαρχει
                    assertEquals(4, cache.get(4));  //key 4 πρεπει να υπαρχει
                
                
            }
            
        }
    }

        @Test
    public void testEmptyCache() {
        MyCache<Integer, Integer> cache;
        for (CacheReplacementPolicy p: CacheReplacementPolicy.values()){
            cache=new MyCache(3,p);
            //παιρνω null εφοσον ειναι αδειο το cache
            assertEquals(null,cache.get(1));
        }    
    }
    
        @Test
    void testStress() {
        int capacity = 100;
        MyCache<Integer, Integer> cache;
        
        for (CacheReplacementPolicy p: CacheReplacementPolicy.values()){
            cache=new MyCache(capacity,p);
            // προσθετω τα διπλασια στοιχεια απο οσα χωραει
            for (int i = 0; i < capacity * 2; i++) {
                cache.put(i, i * 10);
                // ελεγχω οτι μπαινουν σωστα
                assertEquals(i * 10, cache.get(i));
            }
            //κλειδια mru: {0-98,199} values mru: {0,980,1990}
            
            for (int i = 0; i < capacity; i++) {
                if (p==CacheReplacementPolicy.LRU){
                    // τα παλια στοιχεια εχουν ολα διαγραφει
                    assertEquals(null,cache.get(i));
                    //στοιχεια με κλειδια απο 0 εως και 99 εχουν αντικατασταθει
                }else if (p==CacheReplacementPolicy.MRU){
                    //υπαρχουν τα στοιχεια με κλειδι 0-98
                    if (i<=98){
                        assertEquals(i*10,cache.get(i));
                    }else{
                        assertEquals(null,cache.get(i));
                        assertEquals(1990,cache.get(199));
                    }
                }else if (p == CacheReplacementPolicy.LFU) {
                    if (i < 100) {
                        // στοιχεια με κλειδια απο 0 εως και 99 εχουν αντικατασταθει
                        assertEquals(null, cache.get(i));
                    } else {
                        // στοιχεια με κλειδι απο 100 εως και 199 υπαρχουν
                        assertEquals(i * 10, cache.get(i)); 
                    }
                }
                
            }
            
            for (int i = capacity; i < (capacity * 2)-1; i++) {
                if (p==CacheReplacementPolicy.LRU){
                    // τα καινουργια στοιχεια υπαρχουν
                    assertEquals(i * 10, cache.get(i));
                    //στοιχεια με κλειδι απο 100 εως και 199 υπαρχουν
                }else if (p==CacheReplacementPolicy.MRU){
                    // τα καινουργια δεν στοιχεια υπαρχουν
                    if (i!=199){
                        assertEquals(null, cache.get(i));
                    }else {
                        assertEquals(1990,cache.get(i));
                    }
                }else if (p == CacheReplacementPolicy.LFU) {
                    if (i < (capacity + 99)) {
                        assertEquals(i * 10, cache.get(i));  //στοιχεια με κλειδι απο 100 εως και 199 υπαρχουν
                    } else {
                        assertEquals(null, cache.get(i));  // στοιχεια με κλειδια απο 0 εως και 99 εχουν αντικατασταθει
                    }
                }
            }
        }
    }    



    
    
    
    
    
    
}
