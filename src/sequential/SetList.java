package sequential;/* Represent a set of integers as a list of ordered nodes
   with operations to add, remove, check membership, and print.
   At creation, we have two sentinel nodes. Integers are supposed
   to be greater than MIN_VALUE and smaller than MAX_VALUE. */


public class SetList{

    private Node head;

    public SetList(){     
	head = new Node(Integer.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE);
    }

    public boolean add(int item){
	Node pred=head;
        Node curr=head.next;
        while (curr.key < item){
	    pred = curr;
	    curr = pred.next;}
        if (curr.key==item){return false;}
        else {Node node = new Node(item);
	    node.next=curr;
            pred.next=node;
            return true;}}
        
    public boolean remove(int item){
	Node pred=head;
        Node curr=head.next;
        while (curr.key < item){
	    pred = curr;
	    curr = pred.next;}
        if (curr.key==item){pred.next=curr.next; return true;}
        else {return false;}}

    public boolean member(int item){
	Node pred=head;
        Node curr=head.next;
        while (curr.key < item){
	    pred = curr;
	    curr = pred.next;}
        if (curr.key==item){return true;}
        else {return false;}}

    public void print(){
	Node pred=head;
        Node curr=head.next;
        while (curr.next != null){System.out.print(curr.key+" "); curr=curr.next;}
        System.out.println("");}
}