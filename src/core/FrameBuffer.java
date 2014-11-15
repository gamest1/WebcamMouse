package core;

public class FrameBuffer {
	private class Node {
		private Node next = null;
		private Node prev = null;
		private Frame frame = null;
		
		public Node(Frame frame) {
			this.frame = frame;
		}
	}
	
	private static FrameBuffer instance = null;
	private Node head = null;
	private Node tail = null;
	
	private FrameBuffer() {
	}
	
	public static FrameBuffer getInstance() {
		if (instance == null) {
			instance = new FrameBuffer();
		}
		return instance;
	}
	
	public Frame getNext() {
		if (head == null) {
			return null;
		}
		return head.frame;
	}
	
	public Frame popNext() {
		if (head == null) {
			return null;
		}
		Frame out = head.frame;
		if (head == tail) {
			head = null;
			tail = null;
		}
		else {
			Node tmp = head.next;
			head.next = null;
			tmp.prev = null;
			head = tmp;
		}
		return out;
	}
	
	public boolean hasNext() {
		return (head!=null);
	}
	
	public void add(Frame frame) {
		if (head == null) {
			head = new Node(frame);
			tail = head;
		}
		else {
			Node tmp = tail;
			tail = new Node(frame);
			tmp.next = tail;
			tail.prev = tmp;
		}
	}
}
