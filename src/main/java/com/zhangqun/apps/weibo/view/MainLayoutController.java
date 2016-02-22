package com.zhangqun.apps.weibo.view;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BiconnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhangqun.apps.weibo.MainApp;
import com.zhangqun.apps.weibo.Utils;
import com.zhangqun.apps.weibo.model.Follow;
import com.zhangqun.apps.weibo.model.Weibo;
import com.zhangqun.apps.weibo.service.FollowService;
import com.zhangqun.apps.weibo.service.WeiboService;
import com.zhangqun.apps.weibo.view.RootLayoutController.STATUS;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

//@Component
public class MainLayoutController {

	@FXML
	private TextField followsDataField;

	@FXML
	private TextField weibosDataField;

	@FXML
	private Button selectFollowsButton;

	@FXML
	private Button selectWeibosButton;

	@FXML
	private Slider qValueSlider;

	@FXML
	private ComboBox<Integer> kValueCombo;

	@FXML
	private Label qLabel;

	@FXML
	private Button drawRawGraphButton;

	@FXML
	private Button drawDirectedGraphButton;

	@FXML
	private Button drawUndirectedGraphButton;

	@FXML
	private Button discoverKeyUserButton;

	@FXML
	private VBox basicLeftPane;

	@FXML
	private StackPane orginalGraphStackPane;

	private MainApp mainApp;
	private ResourceBundle i18nBundle;

	private File followsFile, weibosFile;
	private double qValue;
	private int kValue;
	private int sum;
	private List<Follow> follows = new ArrayList<>();
	private List<Weibo> weibos = new ArrayList<>();
	private Map<FollowEntry, Double> followsCache = new HashMap<>();
	private Map<FollowEntry, Double> followsEdgeCache = new HashMap<>();
	private List<String> nodes;
	private boolean isPreprocessed;
	private Map<String, Integer> weibosCache = new HashMap<>();
	private Map<String, Double> nodeWeightCache = new HashMap<>();
	private Map<String, Double> personWeightPercentageCache = new HashMap<>();

	@Autowired
	private FollowService followService;
	@Autowired
	private WeiboService weiboService;

	private Logger logger = Logger.getLogger(MainLayoutController.class);

	private Thread mainAlgorithmThread, udUcAlgorithmThread;

	public MainLayoutController() {

	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		i18nBundle = mainApp.getI18nResourceBundle();
	}

	@FXML
	private void initialize() {
		updateQLabel(qValueSlider.getValue());
		qValueSlider.valueProperty().addListener(
				(observable, oldValue, newValue) -> {
					double udValue = newValue.doubleValue();
					updateQLabel(udValue);
				});
		updateActionButtons();
	}

	private void updateQLabel(double value) {
		qValue = value;
		qLabel.setText(String.valueOf(String.format("%.2f", qValue)));
	}

	private void enableInputTabContent(boolean enable) {

	}

	@FXML
	void handleSelectFollowsFile(ActionEvent event) {
		File file = chooserFile();
		if (file != null) {
			String path = file.getPath();
			followsDataField.setText(path);
			followsFile = file;
			mainApp.updateStatusBar(STATUS.FINISH_ALGORITHM_GENERAL);
			updateActionButtons();
		}
	}

	@FXML
	void handleSelectWeibosFile(ActionEvent event) {
		File file = chooserFile();
		if (file != null) {
			String path = file.getPath();
			weibosDataField.setText(path);
			weibosFile = file;
			mainApp.updateStatusBar(STATUS.FINISH_ALGORITHM_GENERAL);
			updateActionButtons();
		}
	}

	private File chooserFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(i18nBundle
				.getString("app.dialog.choose_graph_data.title"));
		String initialOpenLocation = getInitialFileChooserDir();
		fileChooser.setInitialDirectory(new File(initialOpenLocation));
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("TXT", "*.txt"),
				new FileChooser.ExtensionFilter("All Files", "*.*"));
		File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
		return file;
	}

	private String getInitialFileChooserDir() {
		String initialOpenLocation = null;
		try {
			String executingFilePath = MainApp.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath();
			String executingDirPath = new File(executingFilePath).getParent();
			initialOpenLocation = URLDecoder.decode(executingDirPath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (initialOpenLocation == null) {
			initialOpenLocation = System.getProperty("user.home");
		}
		return initialOpenLocation;
	}

	@FXML
	void handleChooseKValue(ActionEvent event) {
		kValue = kValueCombo.getValue();
	}

	private void preprocessIfNeeded() {
		if (!isPreprocessed) {
			// parse follows and weibos file
			parseFileData();

			// update kValueCombo
			List<Integer> allAvailableKValues = new ArrayList<>();
			int nodeSize = weibos.size();
			for (int i = 0; i < nodeSize; i++) {
				allAvailableKValues.add(i + 1);
			}
			kValueCombo.setItems(FXCollections
					.observableList(allAvailableKValues));

			// insert parsed data into database
			followService.insert(follows);
			weiboService.insert(weibos);

			sum = weiboService.calculateStatusSum();
			buildFollowsCache();
			buildWeibosCache();
			// calculate edge weight
			calculateFollowEdgeWeight();

			// build nodes cache
			nodes = weibos.stream().map(w -> w.getPerson())
					.collect(Collectors.toList());
			isPreprocessed = true;
		}
	}

	private void parseFileData() {
		List<String> lines = null;
		Follow follow;
		Weibo weibo;
		try {
			// follows file
			lines = Files.readAllLines(Paths.get(followsDataField.getText()),
					Charset.forName("UTF-8"));
			for (String line : lines) {
				if (!line.trim().startsWith("#")) {
					String[] lineParts = line.split("\\s+");
					if (lineParts.length == 3) {
						follow = new Follow(lineParts[0], lineParts[1],
								Integer.parseInt(lineParts[2]));
						follows.add(follow);
					} else {
						logger.error(String.format(
								"follows file contains illegal entry: %s !",
								line));
					}
				} else {
					logger.info(String.format(
							"follows file skipping comment entry: %s !", line));
				}
			}
			// weibos file
			lines = Files.readAllLines(Paths.get(weibosDataField.getText()),
					Charset.forName("UTF-8"));
			for (String line : lines) {
				if (!line.trim().startsWith("#")) {
					String[] lineParts = line.split("\\s+");
					if (lineParts.length == 2) {
						weibo = new Weibo(lineParts[0],
								Integer.parseInt(lineParts[1]));
						weibos.add(weibo);
					} else {
						logger.error(String.format(
								"weibos file contains illegal entry: %s !",
								line));
					}
				} else {
					logger.info(String.format(
							"weibos file skipping comment entry: %s !", line));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void calculateFollowEdgeWeight() {
		for (Follow follow : follows) {
			double wij = 0;
			int sumi = weibosCache.get(follow.getPerson());
			double comij = followsCache.get(new FollowEntry(follow.getPerson(),
					follow.getFollower()));
			int comj = followService.findTotalInteractivesByFollower(follow
					.getFollower());
			followsEdgeCache.put(
					new FollowEntry(follow.getPerson(), follow.getFollower()),
					sumi * comij * 1.0 / (sum * comj));
		}
	}

	private void buildFollowsCache() {
		for (Follow follow : follows) {
			followsCache.put(
					new FollowEntry(follow.getPerson(), follow.getFollower()),
					Double.valueOf(follow.getInteractives()));
		}
	}

	private void buildWeibosCache() {
		for (Weibo weibo : weibos) {
			weibosCache.put(weibo.getPerson(), weibo.getStatuses());
		}
	}

	@FXML
	void handleDrawRawGraph(ActionEvent event) {
		new Thread(() -> {
			preprocessIfNeeded();
			// draw raw graph
				Platform.runLater(() -> {
					Utils.drawDirectedGraph(orginalGraphStackPane, nodes,
							followsCache, true);
				});
			}).start();
	}

	@FXML
	void handleDrawDirectedGraph(ActionEvent event) {
		new Thread(() -> {
			preprocessIfNeeded();
			// draw raw graph
				Platform.runLater(() -> {
					Utils.drawDirectedGraph(orginalGraphStackPane, nodes,
							followsEdgeCache, false);
				});
			}).start();
	}

	@FXML
	void handleDrawUndirectedGraph(ActionEvent event) {
		new Thread(
				() -> {
					preprocessIfNeeded();
					for (String node : nodes) {
						double wai = calculateWeight(node);
						nodeWeightCache.put(node, wai);
					}
					Set<UndirectedPair> followsEdgeSet = filterMultiEdge(followsEdgeCache);
					Platform.runLater(() -> {
						Utils.drawUndirectedGraph(orginalGraphStackPane,
								nodeWeightCache, followsEdgeSet);
					});
				}).start();
	}

	@FXML
	void handleDiscoverKeyUser(ActionEvent event) {
		new Thread(() -> {
			// prepare initial nodes and edges
			preprocessIfNeeded();
			Set<String> nodeSet = new HashSet<>(nodes);
			Set<UndirectedPair> edgeSet = filterMultiEdge(followsEdgeCache);
			Map<String, Double> nodeWeightMap = rebuildNodeWeight(nodeSet);
			// key nodes collection
			List<String> keyNodes = new ArrayList<>();
			// discover k key nodes
			for(int i = 0; i < kValue; i++) {
				// build undirected graph
				UndirectedGraph<String, DefaultEdge> graph = buildUndirectedGraph(nodeSet, edgeSet);
				// find key node
				BiconnectivityInspector<String, DefaultEdge> graphForInspector = new BiconnectivityInspector<>(graph);
				Set<String> cutpoints = graphForInspector.getCutpoints();
				logger.info(String.format("find current cutpoints: %s !", cutpoints));
				String keyNode = findKeyNode(cutpoints, nodeWeightMap);
				logger.info(String.format("find key node: %s !", keyNode));
				keyNodes.add(keyNode);
				// delete current found key node
				nodeSet.remove(keyNode);
				edgeSet = removeReleventEdge(edgeSet, keyNode);
				nodeWeightMap = rebuildNodeWeight(nodeSet);
			}
		}).start();
	}

	private Map<String, Double> rebuildNodeWeight(Set<String> nodeSet) {
		Map<String, Double> nodeWeightMap = new HashMap<>();
		for (String node : nodeSet) {
			double wai = calculateWeight(node);
			nodeWeightMap.put(node, wai);
		}
		return nodeWeightMap;
	}

	private Set<UndirectedPair> removeReleventEdge(Set<UndirectedPair> edgeSet, String node) {
		Set<UndirectedPair> filtedEdgeSet = new HashSet<>();
		for(UndirectedPair edge : edgeSet) {
			if(!edge.getSource().equals(node) && !edge.getTarget().equals(node)) {
				filtedEdgeSet.add(edge);
			}
		}
		return filtedEdgeSet;
	}

	private String findKeyNode(Set<String> cutpoints, Map<String, Double> nodeWeightMap) {
		String keyNode = null;
		double currentMaxWeight = Double.MIN_VALUE;
		for(String node : cutpoints) {
			double nodeWeight = nodeWeightMap.get(node);
			if(nodeWeight > currentMaxWeight) {
				currentMaxWeight = nodeWeight;
				keyNode = node;
			}
		}
		return keyNode;
	}

	private UndirectedGraph<String, DefaultEdge> buildUndirectedGraph(Set<String> nodeSet,
			Set<UndirectedPair> edgeSet) {
		UndirectedGraph<String, DefaultEdge> graph =
	            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		for(String node : nodeSet) {
			graph.addVertex(node);
		}
		for(UndirectedPair edge : edgeSet) {
			graph.addEdge(edge.getSource(), edge.getTarget());
		}
		return graph;
	}

	private Set<UndirectedPair> filterMultiEdge(
			Map<FollowEntry, Double> followsEdgeCache) {
		Set<UndirectedPair> undirectedEdgeSet = new HashSet<>();
		for (Entry<FollowEntry, Double> edge : followsEdgeCache.entrySet()) {
			FollowEntry followEntry = edge.getKey();
			undirectedEdgeSet.add(new UndirectedPair(followEntry.getPerson(),
					followEntry.getFollower()));
			;
		}
		return undirectedEdgeSet;
	}

	private double calculateWeight(String person) {
		double lastPersonWeight = 1;
		double personWeight = 1;
		boolean isFirstCalculation = true;
		do {
			double personWeightPercentageSum = calculatePersonWeightPercentageSum(person);
			lastPersonWeight = personWeight;
			personWeight = qValue + (1 - qValue) * personWeightPercentageSum
					* lastPersonWeight;
			logger.debug(String
					.format("calculation person %s weight, lastPersonWeight = %f, personWeight = %f",
							person, lastPersonWeight, personWeight));
		} while (personWeight > lastPersonWeight);

		return personWeight;
	}

	private double calculatePersonWeightPercentageSum(String person) {
		double personWeightPercentageSum = 0;
		if (!personWeightPercentageCache.containsKey(person)) {
			List<String> followers = followService.findFollower(person);
			for (String follower : followers) {
				double personWeightPercentage = 0;
				List<String> followdPersons = followService
						.findFollowedPerson(follower);
				double followdPersonWeight = followsEdgeCache
						.get(new FollowEntry(person, follower));
				double followdPersonWeightSum = 0;
				for (String followdPerson : followdPersons) {
					followdPersonWeightSum += followsEdgeCache
							.get(new FollowEntry(followdPerson, follower));
				}
				personWeightPercentageSum += followdPersonWeight
						/ followdPersonWeightSum;
			}
			personWeightPercentageCache.put(person, personWeightPercentageSum);
			return personWeightPercentageSum;
		} else {
			return personWeightPercentageCache.get(person);
		}
	}

	private void updateActionButtons() {
		if (allInputAvailable()) {
			enableExecutionButton(true);
		} else {
			enableExecutionButton(false);
		}
	}

	private void enableExecutionButton(boolean enable) {
		drawRawGraphButton.setDisable(!enable);
		drawDirectedGraphButton.setDisable(!enable);
		drawUndirectedGraphButton.setDisable(!enable);
		discoverKeyUserButton.setDisable(!enable);
	}

	private boolean allInputAvailable() {
		if (!followsDataField.getText().equals("")
				&& !weibosDataField.getText().equals("")) {
			return true;
		}
		return false;
	}

	public void setFollowService(FollowService followService) {
		this.followService = followService;
	}

	public void setWeiboService(WeiboService weiboService) {
		this.weiboService = weiboService;
	}

}
