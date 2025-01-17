package org.hua.ergasiadomes;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

public class MyCache <K,V> implements Cache<K,V> {
    private int capacity;
    private HashMap<K,Node<K,V>> map;
    private DoubleLinkedList<K,V> list;
    //for second part
    private int hitCount;
    private int missCount;
    private int totalOperations;
    private CacheReplacementPolicy policy;
    //for third part
    private TreeMap <Node<K,V>,Integer> treeMap;
    
    private static class FreqComparator <K,V> implements Comparator <Node<K,V>>{
        @Override
        public int compare (Node <K,V> node1, Node<K,V> node2){
            if (node1.getTimesUsed()>node2.getTimesUsed()){
                return 1;
            }else if (node1.getTimesUsed()<node2.getTimesUsed()){
                return -1;
            }else {
                return node1.key.hashCode() - node2.key.hashCode();
            }
        }
        
    }
    
    public MyCache (int capacity, CacheReplacementPolicy policy){
        this.capacity=capacity;
        this.map=new HashMap<>();
        this.list=new DoubleLinkedList<>();
        //for second part
        this.hitCount=0;
        this.missCount=0;
        this.totalOperations=0;
        this.policy=policy;
        //third part
        if (policy==CacheReplacementPolicy.LFU){
            this.treeMap=new TreeMap<Node<K,V>,Integer> (new FreqComparator());
        }
    }
    private static class Node <K,V>{
        Node <K,V> previous;
        Node <K,V> next;
        K key;
        V value;
        private int timesUsed=0;
        
        public Node (K key, V value){
            this.key=key;
            this.value=value;
        }
        public void incrementFreq(){
            timesUsed++;
        }
        public int getTimesUsed(){
            return timesUsed;
        }
        
        @Override
        public String toString(){
            return("Node("+key+","+value+" freq"+timesUsed+")");
        }
    }
    
    public class DoubleLinkedList <K,V> {
        Node <K,V> head;
        Node <K,V> tail;
        private int size;


        public DoubleLinkedList (){
            this.head=null;
            this.tail=null;
            this.size=0;
        }

        public void addNode(Node node){
            //τον κανει add στο τελος
            if (node ==null){
                throw new IllegalArgumentException("Cannot put a null node\nMethod put returned");
            }
            if (head==null){
                head=node;
                tail=node;
                node.next=null;
                node.previous=null;
            }else {
                tail.next=node;
                node.previous=tail;
                tail=node;
            }
            size++;
        }
        public Node<K,V> deleteFirst(){
            if (head==null){
                //εαν ειναι κενη η λιστα
                return null;
            }
            Node <K,V> result=head;
            if (head.next==null){
                //εαν η λιστα εχει μονο εναν
                head=null;
                tail=null;
            }else {
                head=head.next;
                head.previous=null;
                result.next=null;
            }
            size--;
            return result;
        }
        public void remove(Node node) {
                if (node==head){
                    head=node.next;
                    head.previous=null;
                }else if(node==tail){
                    //κανω remove το head
                    tail=node.previous;
                    tail.next=null;
                }else {
                    // κανω remove ενδιαμεσο
                    node.previous.next=node.next;
                    node.next.previous=node.previous;
                }
                node.previous=null;
                node.next=null;
                size --;
        }
        public void moveToLast(Node <K,V> node){
            if (node==tail){
                return;
                //ειμαι ηδη στο τελος
            }
            remove(node);
            addNode(node);
        }
        
        public Node<K,V> deleteLast(){
            if (tail==null){
                return null;
            }else{
                Node <K,V> result=tail;
                size --;
                tail=tail.previous;
                tail.next=null;
                result.previous=null;
                return result;
            }
        }
    }
    
    
    @Override
    public V get(K key) {
        totalOperations++;
        if (!map.containsKey(key)){
            //second part implementation
            missCount++;
            return null;
        }else {
            Node <K,V> node=map.get((K)key);
            list.moveToLast(node);
            //second part implementation
            hitCount++;
            //third part implementation 
            if (policy==CacheReplacementPolicy.LFU){
                treeMap.remove(node);
                node.incrementFreq();
                treeMap.put(node, node.getTimesUsed());
            }
            
            
            return node.value;
        }
    }
    

    @Override
    public void put(K key, V value) {
            if (key==null && value==null){
                throw new IllegalArgumentException("Cannot put a null node\nMethod put returned");
            }
            Node <K,V> existNode=map.get(key);
            
            if (existNode==null){
                //δεν υπαρχει ο κομβος με αυτο το key
                Node<K,V>node=new Node(key,value);
                if (capacity==list.size){
                    Node<K,V>nodeToDelete=null;
                    if (policy==CacheReplacementPolicy.LRU){
                        nodeToDelete=list.deleteFirst();
                    }else if (policy== CacheReplacementPolicy.MRU){
                        nodeToDelete=list.deleteLast();
                    }else if(policy==CacheReplacementPolicy.LFU){
                        if (!treeMap.isEmpty()) {
                           nodeToDelete = treeMap.pollFirstEntry().getKey(); // Αφαιρεί το node από το TreeMap
                           list.remove(nodeToDelete);
                        }               
                    }
                    map.remove(nodeToDelete.key);
                }
                map.put((K)key, node);
                list.addNode(node);
                if (policy==CacheReplacementPolicy.LFU){
                    node.incrementFreq();
                    treeMap.put(node, node.getTimesUsed());
                }
                
            }else{
                //εαν υπαρχει κομβος με αυτο το key
                //ενημερωση του χαρτη
                existNode.value=(V)value;
                list.moveToLast(existNode);
                if (policy==CacheReplacementPolicy.LFU){
                    treeMap.remove(existNode);
                    existNode.incrementFreq();
                    treeMap.put(existNode, existNode.getTimesUsed());
                }
                

            }
           
    }
    public int getTotalOperations(){return totalOperations;}
    public int getHitCount(){return hitCount;}
    public int getMissCount(){return missCount;}
    public void printTotalOperations(){
        System.out.println("Total operations: "+totalOperations+"\nCache hits: "+getHitCount()+"\nCache Misses: "+getMissCount());
        System.out.printf("Hit Rate: %.2f%%  \nMiss Rate: %.2f%%\n",100*(double)getHitCount()/(double) totalOperations ,100*(double)getMissCount()/(double)totalOperations);
    }




}
