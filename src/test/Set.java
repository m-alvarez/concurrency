package test;

import java.util.List;

public interface Set
{
    public boolean add(int item);
    public boolean remove(int item);
    public boolean member(int item);
    public List<Integer> asList();
}
