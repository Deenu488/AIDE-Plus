package io.github.zeroaicy.aide.utils;


import android.text.TextUtils;
import com.aide.common.AppLog;
import com.aide.ui.services.AssetInstallationService;
import com.aide.ui.util.ArtifactNode;
import com.aide.ui.util.BuildGradle;
import com.aide.ui.util.Configuration;
import com.aide.ui.util.FileSystem;
import groovyjarjarantlr.collections.AST;
import io.github.zeroaicy.util.Log;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;

public class ZeroAicyBuildGradle extends BuildGradle {
	public static class DependencyExt extends Dependency {
		public static final int CompileOnly = 0x1;
		public static final int RuntimeOnly = 0x2;
		public static final int LibgdxNatives = 0x3;
		
		public final int type;
		public final Dependency dependency;
		public DependencyExt(int type, Dependency dependency) {
			super(dependency.line);
			this.type = type;
			this.dependency = dependency;
			
			
			//final var LibgdxNatives2 = 0x3;
		
		}
		public boolean isCompileOnly() {
			return this.type == CompileOnly;
		}
		public boolean isRuntimeOnly() {
			return this.type == RuntimeOnly;
		}
		public boolean isLibgdxNatives() {
			return this.type == LibgdxNatives;
		}
		public static boolean isCompileOnly(int type) {
			return type == CompileOnly;
		}
		public static boolean isRuntimeOnly(int type) {
			return type == RuntimeOnly;
		}
		public static boolean isLibgdxNatives(int type) {
			return type == LibgdxNatives;
		}
		public static boolean isExt(int type) {
			return type == CompileOnly || type == RuntimeOnly || type == LibgdxNatives;
		}

	}

	private static String TAG = "ZeroAicyBuildGradle";

	/**
	 * new ProductFlavor统一创建点，方便统一替换实例
	 */
	public ZeroAicyProductFlavor makeProductFlavor() {
		return new ZeroAicyProductFlavor();
	}
	public class ZeroAicyProductFlavor extends ProductFlavor {
		public final List<BuildGradle.Dependency> productFlavorDependencies = new ArrayList<>();
	}

	private static ZeroAicyBuildGradle singleton;
	/**
	 * 单例
	 */
	public static synchronized ZeroAicyBuildGradle getSingleton() {
		if (singleton == null) {
			singleton = new ZeroAicyBuildGradle(true);

			Log.d("ZeroAicyBuildGradle",  "替换gradle解析器");
		}
		return singleton;
	}
	// xxx project(xxxxx)依赖
	private List<ProjectDependency> projectDependencys = new ArrayList<>();
	public List<ProjectDependency> getProjectDependencys() {
		return this.projectDependencys;
	}
	private List<DependencyExt> dependencyExts = new ArrayList<>();
	public List<DependencyExt> getDependencyExts() {
		return this.dependencyExts;
	}

	/**
	 * 混淆
	 */
	private boolean minifyEnabled;
	private boolean shrinkResources;
	private List<String> proguardFiles;

	public boolean isMinifyEnabled() {
		return this.minifyEnabled;
	}

	public boolean isShrinkResources() {
		return this.shrinkResources;
	}
	public List<String> getProguardFiles() {
		if (this.proguardFiles == null) {
			return Collections.emptyList();
		}
		return this.proguardFiles;
	}
	private final boolean isSingleton;
	public boolean isSingleton() {
		return this.isSingleton;
	}
	@Override
	public ZeroAicyBuildGradle getConfiguration(String path) {
		return (ZeroAicyBuildGradle) super.getConfiguration(path);
	}
    public ZeroAicyBuildGradle makeConfiguration(String path) {
		return new ZeroAicyBuildGradle(path);
    }

	private ZeroAicyBuildGradle(boolean isSingleton) {
		super();
		this.isSingleton = isSingleton;
		init();
    }
	public ZeroAicyBuildGradle(String filePath) {
		super();
		// getConfiguration在调用 makeConfiguration后会赋值
		// 导致解析时无法使用此变量, 赋值
		this.configurationPath = filePath;
		this.isSingleton = false;
		init();

		try {
			FileReader fileReader = new FileReader(filePath);
			SourceBuffer sourceBuffer = new SourceBuffer();
			UnicodeEscapingReader unicodeEscapingReader = new UnicodeEscapingReader(fileReader, sourceBuffer);
			GroovyLexer groovyLexer = new GroovyLexer(unicodeEscapingReader);
			unicodeEscapingReader.setLexer(groovyLexer);

			GroovyRecognizer groovyRecognizer = GroovyRecognizer.make(groovyLexer);
			groovyRecognizer.setSourceBuffer(sourceBuffer);
			groovyRecognizer.compilationUnit();
			fileReader.close();

			for (AST ast = groovyRecognizer.getAST(); ast != null; ast = getNextSibling(ast)) {
				nw(ast, "");
			}

			// 打印
			//Log.d(TAG, "signingConfigMap", signingConfigMap);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }

	private void init() {
		this.defaultConfigProductFlavor = makeProductFlavor();
		this.curAndroidNodeLine = -1;
		this.curFlavorsNodeLine = -1;
		this.productFlavorMap = new TreeMap<>();
		this.curProjectsRepositorys = new ArrayList<>();
		this.allProjectsRepositorys = new ArrayList<>();
		this.subProjectsRepositorys = new ArrayList<>();
		this.curDependenciesNodeLine = -1;
		this.dependencies = new ArrayList<>();
		this.subProjectsDependencies = new ArrayList<>();
		this.allProjectsDependencies = new ArrayList<>();
		this.signingConfigMap = new HashMap<>();
	}

	// 是否是 parentNodeName的子节点
    private boolean isChildNode(String nodeName, String parentNodeName) {
		return nodeName.startsWith(parentNodeName + ".");
    }

    private String getValue(AST ast, String nodeName) {
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		if (getType(XL) == 33 
			&& 
			getType(getFirstChild(XL)) == 27 
			&& nodeName.equals(getText(getFirstChild(getFirstChild(XL))))) {
			AST Ws = getFirstChild(getNextSibling(getFirstChild(getFirstChild(XL))));
			if (getType(Ws) == 28) {
				return getText(getFirstChild(Ws));
			}
			return null;
		}
		return null;
    }

    private void FH(String str, String str2, int i) {
		try {
			ArrayList<String> arrayList = new ArrayList<>();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(((Configuration) this).configurationPath));
			int i2 = 1;
			while (true) {
				String readLine = bufferedReader.readLine();
				if (readLine == null) {
					break;
				}
				arrayList.add(readLine);
				if (i2 == i) {
					arrayList.add(str);
				}
				i2++;
			}
			if (i < 0) {
				arrayList.add(str2 + " {");
				arrayList.add(str);
				arrayList.add("}");
			}
			bufferedReader.close();
			FileWriter fileWriter = new FileWriter(((Configuration) this).configurationPath);
			for (String str3 : arrayList) {
				fileWriter.write(str3);
				fileWriter.write("\n");
			}
			fileWriter.close();
		}
		catch (IOException e) {
			AppLog.v5(e);
		}
    }

    private void Hw(String str) {
		FH("\t" + str, "dependencies", this.curDependenciesNodeLine);
    }

    private String J0(AST ast) {
		if (getType(ast) == 28) {
			if (getType(getFirstChild(ast)) == 27 || getType(getFirstChild(ast)) == 124) {
				return aM(getFirstChild(getFirstChild(ast)));
			}
			return null;
		}
		return null;
    }

    private String getAstValue(AST ast) {
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		if (getType(XL) != 88 && getType(XL) != 199) {
			if (getType(XL) == 33) {
				if (getType(getFirstChild(XL)) == 28) {
					return getText(getFirstChild(getFirstChild(XL)));
				}
				return getText(getFirstChild(XL));
			}
			return null;
		}
		return getText(XL);
    }

    private void parserRepositories(AST ast, String repositorieName, List<Repository> repositorys) {
		// google
		repositorys.add(new RemoteRepository(ast.getLine(), "https://dl.google.com/dl/android/maven2"));
		switch (repositorieName) {
			case "jcenter":
				repositorys.add(new RemoteRepository(ast.getLine(), "https://jcenter.bintray.com"));
				break;
			case "mavenCentral":
				repositorys.add(new RemoteRepository(ast.getLine(), "http://repo.maven.apache.org/maven2"));
				break;
			case "maven.url":
				String url = getAstValue(ast);
				if (TextUtils.isEmpty(url)) {
					break;
				}

				if (url.endsWith("/")) {
					int length = url.length() - 2;
					if (length < 1) {
						break;
					}
					// 规范化repositorieURL
					url = url.substring(0, length);						
				}

				repositorys.add(new RemoteRepository(ast.getLine(), url));
				break;
			case "flatDir.dirs":
				FlatLocalRepository flatLocalRepository = new FlatLocalRepository(ast.getLine());
				flatLocalRepository.flatDir = getAstValue(ast);
				repositorys.add(flatLocalRepository);
				break;
			default: 
				return;
		}
    }

    private String Mr(String str, int i) {
		String[] split = str.split("\\.");
		if (split.length > i) {
			String str2 = "";
			for (int i2 = i; i2 < split.length; i2++) {
				if (str2.length() > 0) {
					str2 = str2 + ".";
				}
				str2 = str2 + split[i2];
			}
			return str2;
		}
		return null;

    }

    private void parserProductFlavor(AST ast, String attributeName, ProductFlavor productFlavor) {
		switch (attributeName) {
			
			case "minSdk":
			case "minSdkVersion":
			case "minSdkVersion.apiLevel":
				productFlavor.minSdkVersion = getAstValue(ast);
				break;
			case "targetSdk":
			case "targetSdkVersion":
			case "targetSdkVersion.apiLevel":
				productFlavor.targetSdkVersion = getAstValue(ast);
				break;
			case "versionCode":
				productFlavor.versionCode = getAstValue(ast);
				break;
			case "versionName":
				productFlavor.versionName = getAstValue(ast);
				break;
			case "packageName":
			case "namespace":
			case "applicationId":
				productFlavor.applicationId = getAstValue(ast);
				break;
			case "multiDexEnabled":
				productFlavor.multiDexEnabled = getAstValue(ast);
				break;
			case "dependencies":
				/**
				 * 为渠道包提供 dependencies块支持
				 */
				if (productFlavor instanceof ZeroAicyProductFlavor) {
					List<BuildGradle.Dependency> productFlavorDependencies = ((ZeroAicyProductFlavor)productFlavor).productFlavorDependencies;
					for (AST dependencieChildAst : u7(ast)) {
						String dependencieChildAstName = J0(dependencieChildAst);
						ei(dependencieChildAst, dependencieChildAstName, productFlavorDependencies);
					}
				}
				break;
		}

    }

    private void SI(AST ast, String str, int i) {
		String productFlavorName = getNodeSimpleNameAt(str, i);
		if (!this.productFlavorMap.containsKey(productFlavorName)) {
			this.productFlavorMap.put(productFlavorName, makeProductFlavor());
		}
		String Mr2 = Mr(str, i + 1);
		if (Mr2 != null) {
			parserProductFlavor(ast, Mr2, this.productFlavorMap.get(productFlavorName));
		}

    }

    private static AST getFirstChild(AST ast) {
		if (ast == null) {
			return null;
		}
		return ast.getFirstChild();

    }

    private static AST getNextSibling(AST ast) {
		if (ast == null) {
			return null;
		}
		return ast.getNextSibling();

    }

    private String aM(AST ast) {

		if (getType(ast) == 90) {
			AST Ws = getFirstChild(ast);
			AST XL = getNextSibling(Ws);
			return aM(Ws) + "." + getText(XL);
		}
		return getText(ast);
    }

    private void cn(AST ast, String str, SigningConfig signingConfig) {
		switch (str) {
			case "storePassword":
				signingConfig.storePassword = getAstValue(ast);
				break;
			case "keyPassword":
				signingConfig.keyPassword = getAstValue(ast);
				break;
			case "keyAlias":
				signingConfig.keyAlias = getAstValue(ast);
				break;
			case "storeFile":
				signingConfig.storeFilePath = getValue(ast, "file");
				break;
		}
    }

	
	public static int getDependencyExtType(String type) {
		switch (type) {
			case "compileOnly":
				return DependencyExt.CompileOnly;
				// 仅打包 看看能不能
				// 不在编译列表，仅在打包列表中
			case "runtimeOnly":
				return DependencyExt.RuntimeOnly;

			case "libgdxNatives":
				return DependencyExt.LibgdxNatives;
			default: 
				return -1;
		}
	}
    private void ei(AST ast, String str, List<Dependency> dependencieList) {
        switch (str) {
			case "testCompile":
			case "androidTestCompile":
				dependencieList.add(new k(ast.getLine()));
				break;
			case "wearApp":
				Map<String, String> we;
				String projectValue = getValue(ast, "project");
				if (projectValue == null && (we = we(ast, "project")) != null && we.containsKey("path")) {
					projectValue = we.get("path");
				}
				if (projectValue != null) {
					ProjectDependency projectDependency = new ProjectDependency(ast.getLine());
					this.wearAppProject = projectDependency;
					projectDependency.projectName = projectValue;
					return;
				}
				dependencieList.add(new l(ast.getLine()));

				break;
				// 仅用于标记依赖
				// 仅加入编译列表
				// 不加入打包列表
			case "compileOnly":
				// 仅打包 看看能不能
				// 不在编译列表，仅在打包列表中
			case "runtimeOnly":
			case "libgdxNatives":

			case "implementation":
			case "api":
			case "compile":
				int dependencyExtType = getDependencyExtType(str);
				{
					{
						// xxx project(:"xx");
						String projectPath = getValue(ast, "project");
						if (projectPath != null) {
							ProjectDependency projectDependency = new ProjectDependency(ast.getLine());
							projectDependency.projectName = projectPath;

							dependencieList.add(projectDependency);
							// 添加项目依赖
							this.projectDependencys.add(projectDependency);
							return;
						}
					}
					
					{
						// xxx files("xx");
						String getFilesValue = getValue(ast, "files");
						if (getFilesValue != null) {
							FilesDependency filesDependency = new FilesDependency(ast.getLine());
							filesDependency.filesPath = getFilesValue;
							
							if( !DependencyExt.isRuntimeOnly(dependencyExtType)){
								// runtimeOnly files 依赖会在打包服务进程自动添加
								// 在此处拦截可以使得编译器不知道这个依赖
								dependencieList.add(filesDependency);
							}
							
							if (DependencyExt.isExt(dependencyExtType)) {
								this.dependencyExts.add(new DependencyExt(dependencyExtType, filesDependency));
							}
							
							return;
						}
					}
					
					{
						// xxx fileTree(dir: "xx");
						Map<String, String> getFileTree = we(ast, "fileTree");
						if (getFileTree != null) {
							FileTreeDependency fileTreeDependency = new FileTreeDependency(ast.getLine());
							//getFileTree.get("include");
							fileTreeDependency.dirPath = getFileTree.get("dir");
							dependencieList.add(fileTreeDependency);

							if (DependencyExt.isExt(dependencyExtType)) {
								this.dependencyExts.add(new DependencyExt(dependencyExtType, fileTreeDependency));
							}

							return;
						}
					}
					
					{
						// xxx groupId:artifactId:version:classifier@extension
						String coords = getAstValue(ast);
						if (coords != null) {
							ArtifactNode mavenDependency = new ArtifactNode(ast.getLine());
							dependencieList.add(mavenDependency);

							if (DependencyExt.isExt(dependencyExtType)) {
								this.dependencyExts.add(new DependencyExt(dependencyExtType, mavenDependency));
							}

							mavenDependency.coords = coords;
							String[] coordsArray = coords.split(":");

							if (coordsArray.length > 0) {
								mavenDependency.groupId = coordsArray[0];
							}
							if (coordsArray.length > 1) {
								mavenDependency.artifactId = coordsArray[1];
							}
							// 正确处理没有version的依赖
							if (coordsArray.length == 2) {
								return;
							}
							
							if (coordsArray.length > 2) {
								// groupId:artifactId:version@extension
								// {group}:{name}:{version}[{:classifier}@{extension}]
								if (coordsArray.length == 3) {
									String version = coordsArray[2];
									int extensionEnd = version.indexOf("@");
									if (extensionEnd >= 0) {
										mavenDependency.version = version.substring(0, extensionEnd);
										mavenDependency.packaging = version.substring(extensionEnd + 1);
										return;
									}
									mavenDependency.version = version;
									return;
								}

								if (coordsArray.length > 3) {
									mavenDependency.version = coordsArray[2];
									String classifier = coordsArray[3];

									int extensionEnd = classifier.indexOf("@");
									if (extensionEnd >= 0) {
										mavenDependency.classifier = classifier.substring(0, extensionEnd);
										mavenDependency.packaging = classifier.substring(extensionEnd + 1);
										return;
									}
									mavenDependency.classifier = classifier;
									return;
								}
							}
						}
					}
					dependencieList.add(new l(ast.getLine()));
				}
				break;
			default: 
				dependencieList.add(new l(ast.getLine()));
				break;
		}
    }

    private String getNodeSimpleNameAt(String str, int index) {
		String[] split = str.split("\\.");
		if (split.length > index) {
			return split[index];
		}
		return null;

    }

    private static String getText(AST ast) {
		if (ast == null) {
			return null;
		}
		return ast.getText();

    }

    private void nw(AST ast, String parentNodeName) {
		String nodeName = J0(ast);
		if (nodeName == null) {
			return;
		}
		if (parentNodeName.length() != 0) {
			nodeName = parentNodeName + "." + nodeName;
		}
		//System.out.printf("解析%s\n", nodeName);

		switch (nodeName) {
			case "android":
			case "model.android":
				this.curAndroidNodeLine = tp(ast);
				break;

			case "android.productFlavors":
			case "model.android.productFlavors":
				this.curFlavorsNodeLine = tp(ast);
				break;
			case "dependencies":
				this.curDependenciesNodeLine = tp(ast);
				break;
			case "android.compileSdkVersion":
			case "model.android.compileSdkVersion":
				getAstValue(ast);
				break;
			default:
				if (isChildNode(nodeName, "android.defaultConfig")) {
					parserProductFlavor(ast, Mr(nodeName, 2), this.defaultConfigProductFlavor);
					break;
				} else if (isChildNode(nodeName, "model.android.defaultConfig")) {
					parserProductFlavor(ast, Mr(nodeName, 3), this.defaultConfigProductFlavor);
					break;
				} else if (isChildNode(nodeName, "model.android.defaultConfig.with")) {
					parserProductFlavor(ast, Mr(nodeName, 4), this.defaultConfigProductFlavor);
					break;
				} else if (isChildNode(nodeName, "android.productFlavors")) {
					SI(ast, nodeName, 2);
					break;
				} else if (isChildNode(nodeName, "model.android.productFlavors")) {
					SI(ast, nodeName, 3);
					break;
				} else if (isChildNode(nodeName, "android.signingConfigs")) {
					ro(ast, nodeName, 2);
					break;
				} else if (isChildNode(nodeName, "model.android.signingConfigs")) {
					ro(ast, nodeName, 3);
					break;
				} else if (isChildNode(nodeName, "dependencies")) {
					ei(ast, Mr(nodeName, 1), this.dependencies);
					break;
				} else if (isChildNode(nodeName, "subprojects.dependencies")) {
					ei(ast, Mr(nodeName, 2), this.subProjectsDependencies);
					break;
				} else if (isChildNode(nodeName, "allprojects.dependencies")) {
					ei(ast, Mr(nodeName, 2), this.allProjectsDependencies);
					break;
				} else if (isChildNode(nodeName, "repositories")) {
					parserRepositories(ast, Mr(nodeName, 1), this.curProjectsRepositorys);
					break;
				} else if (isChildNode(nodeName, "subprojects.repositories")) {
					parserRepositories(ast, Mr(nodeName, 2), this.subProjectsRepositorys);
					break;
				} else if (isChildNode(nodeName, "allprojects.repositories")) {
					parserRepositories(ast, Mr(nodeName, 2), this.allProjectsRepositorys);
					break;
				} else if (isChildNode(nodeName, "android.buildTypes.release")) {
					int i = 3;
					String nodeSimpleName = getNodeSimpleNameAt(nodeName, i);
					if ("minifyEnabled".equals(nodeSimpleName)) {
						this.minifyEnabled = "true".equals(getAstValue(ast));
						break;
					} 
					if ("shrinkResources".equals(nodeSimpleName)) {
						this.shrinkResources = "true".equals(getAstValue(ast));
						break;
					} 
					if (this.proguardFiles == null 
						&& "proguardFiles".equals(nodeSimpleName)) {

						AST nextSibling = getNextSibling(getFirstChild(getFirstChild(ast)));
						if (getType(nextSibling) != 33) {
							break;
						}

						List<String> proguardFiles = new ArrayList<>();

						for (AST firstChild1 = getFirstChild(nextSibling); firstChild1 != null; firstChild1 = getNextSibling(firstChild1)) {
							if (getType(firstChild1) == 88) {
								String proguardFilePath = FileSystem.Qq(FileSystem.getParent(this.configurationPath), getText(firstChild1));
								proguardFiles.add(proguardFilePath);
								continue;
							}
							if (getType(firstChild1) == 27) {
								AST firstChild2 = getFirstChild(firstChild1);

								if (getType(firstChild2) == 87 
									&& "getDefaultProguardFile".equals(getText(firstChild2))) {

									AST firstChild4 = getFirstChild(getFirstChild(getNextSibling(firstChild2)));
									String defaultProguardFile = getDefaultProguardFile(getText(firstChild4));
									proguardFiles.add(defaultProguardFile);
								}
							}
						}
						if (! proguardFiles.isEmpty()) {
							this.proguardFiles = proguardFiles;
						}
					}

					break;
				}
				break;
		}

		for (AST ast2 : u7(ast)) {
			nw(ast2, nodeName);
		}
	}

	private static Set<String> proguards = new HashSet<>();
	static{
		proguards.add("proguard-android.txt");
		proguards.add("proguard-android-optimize.txt");
		proguards.add("proguard-defaults.txt");
	}
	private String getDefaultProguardFile(String proguardFileName) {

		if (proguards.contains(proguardFileName)) {
			return AssetInstallationService.DW(proguardFileName, true);
		}
		return null;
	}


    private static int getType(AST ast) {
		if (ast == null) {
			return 0;
		}
		return ast.getType();

    }

    private void ro(AST ast, String str, int i) {
		String signingConfigName = getNodeSimpleNameAt(str, i);
		if (!this.signingConfigMap.containsKey(signingConfigName)) {
			this.signingConfigMap.put(signingConfigName, new SigningConfig());
		}
		String Mr2 = Mr(str, i + 1);
		if (Mr2 != null) {
			cn(ast, Mr2, this.signingConfigMap.get(signingConfigName));
		}

    }

    private int tp(AST ast) {
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		return XL == null ? ast.getLine() : XL.getLine();

    }

    private List<AST> u7(AST ast) {
		ArrayList<AST> arrayList = new ArrayList<>();
		for (AST Ws = getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast)))); Ws != null; Ws = getNextSibling(Ws)) {
			if (getType(Ws) == 28) {
				arrayList.add(Ws);
			}
		}
		return arrayList;

    }

    private Map<String, String> we(AST ast, String str) {
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		if (getType(XL) == 33 && getType(getFirstChild(XL)) == 27 && str.equals(getText(getFirstChild(getFirstChild(XL))))) {
			HashMap<String, String> hashMap = new HashMap<>();
			for (AST Ws = getFirstChild(getNextSibling(getFirstChild(getFirstChild(XL)))); Ws != null; Ws = getNextSibling(Ws)) {
				if (getType(Ws) == 54) {
					String lg = getText(getFirstChild(Ws));
					AST Ws2 = getFirstChild(getNextSibling(getFirstChild(Ws)));
					if (getType(Ws2) == 57) {
						hashMap.put(lg, getText(getFirstChild(getFirstChild(getFirstChild(Ws2)))));
					} else {
						hashMap.put(lg, getText(Ws2));
					}
				}
			}
			return hashMap;
		}
		return null;

    }

    public void addMavenDependency(String str) {
		Hw("api '" + str + "'");

    }

    public void addProductFlavor(String str) {
		String str2 = "\t\t" + str + " {\n\t\t}";
		if (this.curFlavorsNodeLine != -1) {
			FH(str2, "", this.curFlavorsNodeLine);
			return;
		}
		FH("\tproductFlavors {\n" + str2 + "\n\t}\n", "android", this.curAndroidNodeLine);

    }

    public void addProjectDependency(String str) {
		String BT = FileSystem.BT(FileSystem.getParent(FileSystem.getParent(((Configuration) this).configurationPath)), str);
		Hw("api project('" + (":" + BT.replace("/", ":")) + "')");

    }

	@Override
    public String getFlavorApplicationId(String productFlavorName) {
		if (productFlavorName != null && this.productFlavorMap.containsKey(productFlavorName) && this.productFlavorMap.get(productFlavorName).applicationId != null) {
			return this.productFlavorMap.get(productFlavorName).applicationId;
		}
		return this.defaultConfigProductFlavor.applicationId;
    }

	public List<BuildGradle.Dependency> getFlavorDependencies(String productFlavorName) {
		BuildGradle.ProductFlavor productFlavor;
		if (productFlavorName != null 
			&& (productFlavor = this.productFlavorMap.get(productFlavorName)) != null
			&& productFlavor instanceof ZeroAicyProductFlavor) {
			return ((ZeroAicyProductFlavor)productFlavor).productFlavorDependencies;
		}
		return Collections.emptyList();

    }

	@Override
    public String getMinSdkVersion(String productFlavorName) {
		if (productFlavorName != null && this.productFlavorMap.containsKey(productFlavorName) && this.productFlavorMap.get(productFlavorName).minSdkVersion != null) {
			return this.productFlavorMap.get(productFlavorName).minSdkVersion;
		}
		return this.defaultConfigProductFlavor.minSdkVersion;
    }

	@Override
    public String getTargetSdkVersion(String productFlavorName) {
		if (productFlavorName != null 
			&& this.productFlavorMap.containsKey(productFlavorName) 
			&& this.productFlavorMap.get(productFlavorName).targetSdkVersion != null) {
			return this.productFlavorMap.get(productFlavorName).targetSdkVersion;
		}
		return this.defaultConfigProductFlavor.targetSdkVersion;

    }

    @Override
	public String getVersionCode(String productFlavorName) {
		if (productFlavorName != null
			&& this.productFlavorMap.containsKey(productFlavorName) 
			&& this.productFlavorMap.get(productFlavorName).versionCode != null) {
			return this.productFlavorMap.get(productFlavorName).versionCode;
		}
		return this.defaultConfigProductFlavor.versionCode;

    }

	@Override
    public String getVersionName(String productFlavorName) {
		if (productFlavorName != null 
			&& this.productFlavorMap.containsKey(productFlavorName) 
			&& this.productFlavorMap.get(productFlavorName).versionName != null) {
			return this.productFlavorMap.get(productFlavorName).versionName;
		}
		return this.defaultConfigProductFlavor.versionName;
    }


	@Override
    public SigningConfig getSigningConfig(String signingConfigName) {
		if (signingConfigName == null) {
			return null;
		}
		return this.signingConfigMap.get(signingConfigName);
    }

	@Override
    public boolean isMultiDexEnabled(String productFlavorName) {
		if (productFlavorName != null 
			&& this.productFlavorMap.containsKey(productFlavorName) 
			&& this.productFlavorMap.get(productFlavorName).multiDexEnabled != null) {
			return "true".equals(this.productFlavorMap.get(productFlavorName).multiDexEnabled);
		}
		return "true".equals(this.defaultConfigProductFlavor.multiDexEnabled);
	}


}
