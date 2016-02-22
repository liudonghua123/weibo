package com.zhangqun.apps.weibo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import javax.swing.JComponent;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

import com.zhangqun.apps.weibo.view.FollowEntry;
import com.zhangqun.apps.weibo.view.UndirectedPair;

public class Utils {

	public static String DEFAULT_DELIMITER = ",";

	public static double formatNumber(double doubleValue) {
		return new BigDecimal(doubleValue)
				.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static void enableControl(boolean enable, Control... controls) {
		for (Control control : controls) {
			control.setDisable(!enable);
		}
	}

	public static void drawDirectedGraph(Pane pane, List<String> nodes,
			Map<FollowEntry, Double> followsEdgeCache, boolean isIntegerEdge) {
		Graph graph = new MultiGraph("id");

		System.setProperty("org.graphstream.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		String styleSheet = null;
		try {
			styleSheet = readClassPathFile("/stylesheet.css");
		} catch (IOException e) {
			e.printStackTrace();
		}

		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");

		// 添加节点
		for (String node : nodes) {
			graph.addNode(node);
		}
		// 添加边
		for (Entry<FollowEntry, Double> entry : followsEdgeCache.entrySet()) {
			FollowEntry followEntry = entry.getKey();
			double edgeValue = entry.getValue();
			graph.addEdge(
					isIntegerEdge ? String.format("interactives: %s<-%s: %d",
							followEntry.getPerson(), followEntry.getFollower(),
							(int) edgeValue) : String.format("W%s<-%s: %.2f",
							followEntry.getPerson(), followEntry.getFollower(),
							edgeValue), followEntry.getFollower(), followEntry
							.getPerson(), true);
		}
		// 遍历节点，添加相应属性
		Collection<Node> nodeSet = graph.getNodeSet();
		for (Node node : nodeSet) {
			node.addAttribute("ui.label", node.getId());
		}
		Collection<Edge> edgeSet = graph.getEdgeSet();
		for (Edge edge : edgeSet) {
			edge.addAttribute("ui.label", edge.getId());
		}

		Viewer viewer = new Viewer(graph,
				Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.enableAutoLayout();
		View view = viewer.addDefaultView(false);

		SwingNode swingNode = new SwingNode();
		swingNode.setContent((JComponent) view);
		pane.getChildren().remove(0, pane.getChildren().size());
		pane.getChildren().add(swingNode);
	}

	public static void drawUndirectedGraph(StackPane pane,
			Map<String, Double> nodeWeightCache,
			Set<UndirectedPair> followsEdgeSet) {
		Graph graph = new MultiGraph("id");

		System.setProperty("org.graphstream.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		String styleSheet = null;
		try {
			styleSheet = readClassPathFile("/stylesheet.css");
		} catch (IOException e) {
			e.printStackTrace();
		}

		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");

		// 添加节点
		for (Entry<String, Double> nodeWithWeight : nodeWeightCache.entrySet()) {
			graph.addNode(nodeWithWeight.getKey());
		}
		// 添加边
		for (UndirectedPair entry : followsEdgeSet) {
			graph.addEdge(String.format("%s-%s", entry.getSource(),
					entry.getTarget()), entry.getSource(), entry.getTarget());
		}
		// 遍历节点，添加相应属性
		Collection<Node> nodeSet = graph.getNodeSet();
		for (Node node : nodeSet) {
			node.addAttribute(
					"ui.label",
					String.format("%s: %.2f", node.getId(),
							nodeWeightCache.get(node.getId())));
		}
		Collection<Edge> edgeSet = graph.getEdgeSet();
		for (Edge edge : edgeSet) {
			edge.addAttribute("ui.label", "");
		}

		Viewer viewer = new Viewer(graph,
				Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.enableAutoLayout();
		View view = viewer.addDefaultView(false);

		SwingNode swingNode = new SwingNode();
		swingNode.setContent((JComponent) view);
		pane.getChildren().remove(0, pane.getChildren().size());
		pane.getChildren().add(swingNode);
	}

	private static String readClassPathFile(String fileClassPath)
			throws IOException {
		String styleSheet;
		InputStream is = Utils.class.getResourceAsStream(fileClassPath);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		isr.close();
		is.close();
		styleSheet = sb.toString();
		return styleSheet;
	}

	public static boolean isEmptyCollection(String[] splitStr) {
		if (splitStr == null || splitStr.length == 0) {
			return true;
		} else if (splitStr.length == 1 && splitStr[0].equals("")) {
			return true;
		}
		return false;
	}

	public static int[] convertToIntegerArray(Collection<Integer> collections) {
		int[] result = new int[collections.size()];
		Iterator<Integer> iterator = collections.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			result[i] = iterator.next();
			i++;
		}
		return result;
	}

	// http://stackoverflow.com/questions/23057549/lambda-expression-to-convert-array-list-of-string-to-array-list-of-integers
	// for lists
	public static <T, U> List<U> convertList(List<T> from, Function<T, U> func) {
		return from.stream().map(func).collect(Collectors.toList());
	}

	// for arrays
	public static <T, U> U[] convertArray(T[] from, Function<T, U> func,
			IntFunction<U[]> generator) {
		return Arrays.stream(from).map(func).toArray(generator);
	}

	static class CustomToolTipGenerator implements CategoryToolTipGenerator {
		public String generateToolTip(CategoryDataset dataset, int row,
				int column) {
			return row + ": " + column;
		}
	}

	// http://stackoverflow.com/questions/6247144/how-to-load-a-folder-from-a-jar
	/**
	 * List directory contents for a resource folder. Not recursive. This is
	 * basically a brute-force implementation. Works for regular files and also
	 * JARs.
	 * 
	 * @author Greg Briggs
	 * @param clazz
	 *            Any java class that lives in the same place as the resources
	 *            you want.
	 * @param path
	 *            Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static String[] getResourceListing(Class clazz, String path)
			throws URISyntaxException, IOException {
		URL dirURL = clazz.getClassLoader().getResource(path);
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			/* A file path: easy enough */
			return new File(dirURL.toURI()).list();
		}

		if (dirURL == null) {
			// In case of a jar file, we can't actually find a directory. Have
			// to assume the same jar as clazz.
			String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}
		// A JAR path
		if (dirURL.getProtocol().equals("jar")) {
			// strip out only the JAR file
			String jarPath = dirURL.getPath().substring(5,
					dirURL.getPath().indexOf("!"));
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			// gives ALL entries in jar
			Enumeration<JarEntry> entries = jar.entries();
			// avoid duplicates in case it is a subdirectory
			Set<String> result = new HashSet<String>();
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				// filter according to the path
				if (name.startsWith(path) && !name.equals(path)) {
					String entry = name.substring(path.length());
					int checkSubdir = entry.indexOf("/");
					if (checkSubdir >= 0) {
						// if it is a subdirectory, we just return the directory
						// name
						entry = entry.substring(0, checkSubdir);
					}
					result.add(entry);
				}
			}
			return result.toArray(new String[result.size()]);
		}

		throw new UnsupportedOperationException("Cannot list files for URL "
				+ dirURL);
	}

}
