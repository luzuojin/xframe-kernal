package dev.xframe.http.service.path;

import java.util.HashMap;
import java.util.Map;

import dev.xframe.utils.XStrings;

public class PathMap<T> {

	public static final String WILDCARD = "*";
	
	private Node<T> root = new Node<>(null, "", null);
	private int size;
	
	public T get(String path) {
		Node<T> node = get0(root, path);
		return node == null ? null : node.value;
	}
	
	private Node<T> get0(Node<T> parent, String path) {
		int nd = path.indexOf('/');
		String sub = nd == -1 ? path : path.substring(0, nd);
		Node<T> node = parent.child(sub);
		if(node == null) {//try wildcard
			node = parent.child(WILDCARD);
		}
		return (nd == -1 || node == null) ? node : get0(node, path.substring(nd+1));
	}
	
	public T put(String path, T value) {
		path = XStrings.trim(path, '/');
		T old = XStrings.isEmpty(path) ? root.set(value) : put0(root, path, value);
		if(old == null) ++size;
		return old;
	}
	
	private T put0(Node<T> parent, String path, T value) {
		int nd = path.indexOf('/', 0);
		String sub = nd == -1 ? path : path.substring(0, nd);
		Node<T> node = parent.child(sub);
		if(node == null) {//add child path node
			node = parent.add(sub);
		}
		return nd == -1 ? node.set(value) : put0(node, path.substring(nd+1), value);
	}

	static class Node<E> {
		final Node<E> parent;
		final String path;
		E value;
		Map<String, Node<E>> children;
		public Node(Node<E> parent, String path, E element) {
			this.parent = parent;
			this.path = path;
			this.value = element;
			this.children = new HashMap<>();
		}
		public Node<E> add(String path) {
			Node<E> node = new Node<>(this, path, null);
			children.put(path, node);
			return node;
		}
		E set(E value) {
			E o = this.value;
			this.value = value;
			return o;
		}
		public Node<E> child(String path) {
			return children.get(path);
		}
	}

	public int size() {
		return size;
	}
}
