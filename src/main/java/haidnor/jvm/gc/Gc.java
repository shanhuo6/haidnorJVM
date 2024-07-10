package haidnor.jvm.gc;

import haidnor.jvm.rtda.InstanceArray;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;

import static haidnor.jvm.rtda.Heap.arrayContainer;
import static haidnor.jvm.rtda.Heap.objectContainer;

@Data
@Slf4j
public class Gc {
    private String name;

    private int capacity;

    public Gc(String myGc, int size) {
        this.name = myGc;
        this.capacity = size;
    }

    public Gc() {
    }

    public static  void gc_Object(Gc myGc){

        // 使用迭代器遍历HashMap
        Iterator<Map.Entry<Object, Integer>> iterator = objectContainer.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Integer> entry = iterator.next();

            // 判断值是否为0，如果是则使用迭代器的remove()方法移除该键值对
            if (entry.getValue() == 0) {
                iterator.remove();
                myGc.setCapacity(myGc.getCapacity()-1);
            }
        }

        log.debug("--------------------------------------------------------------------------------------------");
        log.debug("对象容器GC后剩余对象实例：{}",objectContainer);

    }

    public static  void gc_Array(Gc myGc){

        // 使用迭代器遍历HashMap
        Iterator<Map.Entry<InstanceArray, Integer>> iterator = arrayContainer.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<InstanceArray, Integer> entry = iterator.next();

            // 判断值是否为0，如果是则使用迭代器的remove()方法移除该键值对
            if (entry.getValue() == 0) {
                iterator.remove();
                myGc.setCapacity(myGc.getCapacity()-1);
            }
        }

        log.debug("--------------------------------------------------------------------------------------------");
        log.debug("数组容器GC后剩余对象实例：{}",arrayContainer);

    }

}
