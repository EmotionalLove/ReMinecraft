/*
 * Copyright (c) Sasha Stevens 2018.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.sasha.reminecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Sasha on 08/08/2018 at 10:06 AM
 **/
public class YML {

    public static final int DETECT = -1; //Detect by file extension
    public static final int PROPERTIES = 0; // .properties
    public static final int CNF = YML.PROPERTIES; // .cnf
    public static final int JSON = 1; // .js, .json
    public static final int YAML = 2; // .yml, .yaml
    public static final int ENUM = 5; // .txt, .list, .enum
    public static final int ENUMERATION = YML.ENUM;
    public static final Map<String, Integer> format = new TreeMap<>();

    static {
        format.put("properties", YML.PROPERTIES);
        format.put("con", YML.PROPERTIES);
        format.put("conf", YML.PROPERTIES);
        format.put("config", YML.PROPERTIES);
        format.put("js", YML.JSON);
        format.put("json", YML.JSON);
        format.put("yml", YML.YAML);
        format.put("yaml", YML.YAML);
        //format.put("sl", YML.SERIALIZED);
        //format.put("serialize", YML.SERIALIZED);
        format.put("txt", YML.ENUM);
        format.put("list", YML.ENUM);
        format.put("enum", YML.ENUM);
    }

    private final Map<String, Object> nestedCache = new HashMap<>();
    //private LinkedHashMap<String, Object> config = new LinkedHashMap<>();
    private ConfigSection config = new ConfigSection();
    private File file;
    private boolean correct = false;
    private int type = YML.DETECT;

    /**
     * Constructor for Config instance with undefined file object
     *
     * @param type - Config type
     */
    public YML(int type) {
        this.type = type;
        this.correct = true;
        this.config = new ConfigSection();
    }

    /**
     * Constructor for Config (YAML) instance with undefined file object
     */
    public YML() {
        this(YML.YAML);
    }

    public YML(String file) {
        this(file, YML.DETECT);
    }

    public YML(File file) {
        this(file.toString(), YML.DETECT);
    }

    public YML(String file, int type) {
        this(file, type, new ConfigSection());
    }

    public YML(File file, int type) {
        this(file.toString(), type, new ConfigSection());
    }

    @Deprecated
    public YML(String file, int type, LinkedHashMap<String, Object> defaultMap) {
        this.load(file, type, new ConfigSection(defaultMap));
    }

    public YML(String file, int type, ConfigSection defaultMap) {
        this.load(file, type, defaultMap);
    }

    @Deprecated
    public YML(File file, int type, LinkedHashMap<String, Object> defaultMap) {
        this(file.toString(), type, new ConfigSection(defaultMap));
    }

    public void reload() {
        this.config.clear();
        this.nestedCache.clear();
        this.correct = false;
        //this.load(this.file.toString());
        if (this.file == null) throw new IllegalStateException("Failed to reload Config. File object is undefined.");
        this.load(this.file.toString(), this.type);

    }

    public boolean load(String file) {
        return this.load(file, YML.DETECT);
    }

    public boolean load(String file, int type) {
        return this.load(file, type, new ConfigSection());
    }

    @SuppressWarnings("unchecked")
    public boolean load(String file, int type, ConfigSection defaultMap) {
        this.correct = true;
        this.type = type;
        this.file = new File(file);
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
            }
            this.config = defaultMap;
            this.save();
        } else {
            if (this.type == YML.DETECT) {
                String extension = "";
                if (this.file.getName().lastIndexOf(".") != -1 && this.file.getName().lastIndexOf(".") != 0) {
                    extension = this.file.getName().substring(this.file.getName().lastIndexOf(".") + 1);
                }
                if (format.containsKey(extension)) {
                    this.type = format.get(extension);
                } else {
                    this.correct = false;
                }
            }
            if (this.correct) {
                String content = "";
                try {
                    content = FileUtils.readFile(this.file);
                } catch (IOException e) {
                }
                this.parseContent(content);
                if (!this.correct) return false;
                if (this.setDefault(defaultMap) > 0) {
                    this.save();
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean load(InputStream inputStream) {
        if (inputStream == null) return false;
        if (this.correct) {
            String content;
            try {
                content = FileUtils.readFile(inputStream);
            } catch (IOException e) {
                return false;
            }
            this.parseContent(content);
        }
        return correct;
    }

    public boolean loadRaw(String content) {
        if (this.correct) {
            this.parseContent(content);
        }
        return correct;
    }

    public boolean check() {
        return this.correct;
    }

    public boolean isCorrect() {
        return correct;
    }

    /**
     * Save configuration into provided file. Internal file object will be set to new file.
     *
     * @param file
     * @param async
     * @return
     */
    public boolean save(File file, boolean async) {
        this.file = file;
        return save(async);
    }

    public boolean save(File file) {
        this.file = file;
        return save();
    }

    public boolean save() {
        return this.save(false);
    }

    public boolean save(Boolean async) {
        if (this.file == null) throw new IllegalStateException("Failed to save Config. File object is undefined.");
        if (this.correct) {
            StringBuilder content = new StringBuilder();
            switch (this.type) {
                case YML.PROPERTIES:
                    content = new StringBuilder(this.writeProperties());
                    break;
                case YML.JSON:
                    content = new StringBuilder(new GsonBuilder().setPrettyPrinting().create().toJson(this.config));
                    break;
                case YML.YAML:
                    DumperOptions dumperOptions = new DumperOptions();
                    dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    Yaml yaml = new Yaml(dumperOptions);
                    content = new StringBuilder(yaml.dump(this.config));
                    break;
                case YML.ENUM:
                    for (Object o : this.config.entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        content.append(String.valueOf(entry.getKey())).append("\r\n");
                    }
                    break;
                default:
                    break;
            }
            try {
                FileUtils.writeFile(this.file, content.toString());
            } catch (IOException e) {
            }
            return true;
        } else {
            return false;
        }
    }

    public void set(final String key, Object value) {
        this.config.set(key, value);
    }

    public Object get(String key) {
        return this.get(key, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        return this.correct ? this.config.get(key, defaultValue) : defaultValue;
    }

    public ConfigSection getSection(String key) {
        return this.correct ? this.config.getSection(key) : new ConfigSection();
    }

    public boolean isSection(String key) {
        return config.isSection(key);
    }

    public ConfigSection getSections(String key) {
        return this.correct ? this.config.getSections(key) : new ConfigSection();
    }

    public ConfigSection getSections() {
        return this.correct ? this.config.getSections() : new ConfigSection();
    }

    public int getInt(String key) {
        return this.getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return this.correct ? this.config.getInt(key, defaultValue) : defaultValue;
    }

    public boolean isInt(String key) {
        return config.isInt(key);
    }

    public long getLong(String key) {
        return this.getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        return this.correct ? this.config.getLong(key, defaultValue) : defaultValue;
    }

    public boolean isLong(String key) {
        return config.isLong(key);
    }

    public double getDouble(String key) {
        return this.getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue) {
        return this.correct ? this.config.getDouble(key, defaultValue) : defaultValue;
    }

    public float getFloat(String key) {
        return (float) this.getDouble(key, 0);
    }

    public float getFloat(String key, double defaultValue) {
        return (float) (this.correct ? this.config.getDouble(key, defaultValue) : defaultValue);
    }

    public boolean isDouble(String key) {
        return config.isDouble(key);
    }

    public String getString(String key) {
        return this.getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return this.correct ? this.config.getString(key, defaultValue) : defaultValue;
    }

    public boolean isString(String key) {
        return config.isString(key);
    }

    public boolean getBoolean(String key) {
        return this.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.correct ? this.config.getBoolean(key, defaultValue) : defaultValue;
    }

    public boolean isBoolean(String key) {
        return config.isBoolean(key);
    }

    public List getList(String key) {
        return this.getList(key, null);
    }

    public List getList(String key, List defaultList) {
        return this.correct ? this.config.getList(key, defaultList) : defaultList;
    }

    public boolean isList(String key) {
        return config.isList(key);
    }

    public List<String> getStringList(String key) {
        return config.getStringList(key);
    }

    public List<Integer> getIntegerList(String key) {
        return config.getIntegerList(key);
    }

    public List<Boolean> getBooleanList(String key) {
        return config.getBooleanList(key);
    }

    public List<Double> getDoubleList(String key) {
        return config.getDoubleList(key);
    }

    public List<Float> getFloatList(String key) {
        return config.getFloatList(key);
    }

    public List<Long> getLongList(String key) {
        return config.getLongList(key);
    }

    public List<Byte> getByteList(String key) {
        return config.getByteList(key);
    }

    public List<Character> getCharacterList(String key) {
        return config.getCharacterList(key);
    }

    public List<Short> getShortList(String key) {
        return config.getShortList(key);
    }

    public List<Map> getMapList(String key) {
        return config.getMapList(key);
    }

    public void setAll(LinkedHashMap<String, Object> map) {
        this.config = new ConfigSection(map);
    }

    public boolean exists(String key) {
        return config.exists(key);
    }

    public boolean exists(String key, boolean ignoreCase) {
        return config.exists(key, ignoreCase);
    }

    public void remove(String key) {
        config.remove(key);
    }

    public Map<String, Object> getAll() {
        return this.config.getAllMap();
    }

    public void setAll(ConfigSection section) {
        this.config = section;
    }

    /**
     * Get root (main) config section of the Config
     *
     * @return
     */
    public ConfigSection getRootSection() {
        return config;
    }

    public int setDefault(LinkedHashMap<String, Object> map) {
        return setDefault(new ConfigSection(map));
    }

    public int setDefault(ConfigSection map) {
        int size = this.config.size();
        this.config = this.fillDefaults(map, this.config);
        return this.config.size() - size;
    }


    private ConfigSection fillDefaults(ConfigSection defaultMap, ConfigSection data) {
        for (String key : defaultMap.keySet()) {
            if (!data.containsKey(key)) {
                data.put(key, defaultMap.get(key));
            }
        }
        return data;
    }

    private void parseList(String content) {
        content = content.replace("\r\n", "\n");
        for (String v : content.split("\n")) {
            if (v.trim().isEmpty()) {
                continue;
            }
            config.put(v, true);
        }
    }

    private String writeProperties() {
        String content = "#Properties Config file\r\n#" + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + "\r\n";
        for (Object o : this.config.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object v = entry.getValue();
            Object k = entry.getKey();
            if (v instanceof Boolean) {
                v = (Boolean) v ? "on" : "off";
            }
            content += String.valueOf(k) + "=" + String.valueOf(v) + "\r\n";
        }
        return content;
    }

    private void parseProperties(String content) {
        for (String line : content.split("\n")) {
            if (Pattern.compile("[a-zA-Z0-9\\-_\\.]*+=+[^\\r\\n]*").matcher(line).matches()) {
                String[] b = line.split("=", -1);
                String k = b[0];
                String v = b[1].trim();
                String v_lower = v.toLowerCase();
                switch (v_lower) {
                    case "on":
                    case "true":
                    case "yes":
                        this.config.put(k, true);
                        break;
                    case "off":
                    case "false":
                    case "no":
                        this.config.put(k, false);
                        break;
                    default:
                        this.config.put(k, v);
                        break;
                }
            }
        }
    }

    /**
     * @deprecated use {@link #get(String)} instead
     */
    @Deprecated
    public Object getNested(String key) {
        return get(key);
    }

    /**
     * @deprecated use {@link #get(String, T)} instead
     */
    @Deprecated
    public <T> T getNested(String key, T defaultValue) {
        return get(key, defaultValue);
    }

    /**
     * @deprecated use {@link #get(String)} instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <T> T getNestedAs(String key, Class<T> type) {
        return (T) get(key);
    }

    /**
     * @deprecated use {@link #remove(String)} instead
     */
    @Deprecated
    public void removeNested(String key) {
        remove(key);
    }

    private void parseContent(String content) {
        switch (this.type) {
            case YML.PROPERTIES:
                this.parseProperties(content);
                break;
            case YML.JSON:
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                this.config = new ConfigSection(gson.fromJson(content, new TypeToken<LinkedHashMap<String, Object>>() {
                }.getType()));
                break;
            case YML.YAML:
                DumperOptions dumperOptions = new DumperOptions();
                dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                Yaml yaml = new Yaml(dumperOptions);
                this.config = new ConfigSection(yaml.loadAs(content, LinkedHashMap.class));
                break;
            case YML.ENUM:
                this.parseList(content);
                break;
            default:
                this.correct = false;
        }
    }

    public Set<String> getKeys() {
        if (this.correct) return config.getKeys();
        return new HashSet<>();
    }

    public Set<String> getKeys(boolean child) {
        if (this.correct) return config.getKeys(child);
        return new HashSet<>();
    }
}

class ConfigSection extends LinkedHashMap<String, Object> {

    /**
     * Empty ConfigSection constructor
     */
    public ConfigSection() {
        super();
    }

    /**
     * Constructor of ConfigSection that contains initial key/value data
     *
     * @param key
     * @param value
     */
    public ConfigSection(String key, Object value) {
        this();
        this.set(key, value);
    }

    /**
     * Constructor of ConfigSection, based on values stored in map.
     *
     * @param map
     */
    public ConfigSection(LinkedHashMap<String, Object> map) {
        this();
        if (map == null || map.isEmpty()) return;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof LinkedHashMap) {
                super.put(entry.getKey(), new ConfigSection((LinkedHashMap) entry.getValue()));
            } else {
                super.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Get root section as LinkedHashMap
     *
     * @return
     */
    public Map<String, Object> getAllMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.putAll(this);
        return map;
    }


    /**
     * Get new instance of config section
     *
     * @return
     */
    public ConfigSection getAll() {
        return new ConfigSection(this);
    }

    /**
     * Get object by key. If section does not contain value, return null
     */
    public Object get(String key) {
        return this.get(key, null);
    }

    /**
     * Get object by key. If section does not contain value, return default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public <T> T get(String key, T defaultValue) {
        if (key == null || key.isEmpty()) return defaultValue;
        if (super.containsKey(key)) return (T) super.get(key);
        String[] keys = key.split("\\.", 2);
        if (!super.containsKey(keys[0])) return defaultValue;
        Object value = super.get(keys[0]);
        if (value != null && value instanceof ConfigSection) {
            ConfigSection section = (ConfigSection) value;
            return section.get(keys[1], defaultValue);
        }
        return defaultValue;
    }

    /**
     * Store value into config section
     *
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        String[] subKeys = key.split("\\.", 2);
        if (subKeys.length > 1) {
            ConfigSection childSection = new ConfigSection();
            if (this.containsKey(subKeys[0]) && super.get(subKeys[0]) instanceof ConfigSection)
                childSection = (ConfigSection) super.get(subKeys[0]);
            childSection.set(subKeys[1], value);
            super.put(subKeys[0], childSection);
        } else super.put(subKeys[0], value);
    }

    /**
     * Check type of section element defined by key. Return true this element is ConfigSection
     *
     * @param key
     * @return
     */
    public boolean isSection(String key) {
        Object value = this.get(key);
        return value instanceof ConfigSection;
    }

    /**
     * Get config section element defined by key
     *
     * @param key
     * @return
     */
    public ConfigSection getSection(String key) {
        return this.get(key, new ConfigSection());
    }

    //@formatter:off

    /**
     * Get all ConfigSections in root path.
     * Example config:
     * a1:
     * b1:
     * c1:
     * c2:
     * a2:
     * b2:
     * c3:
     * c4:
     * a3: true
     * a4: "hello"
     * a5: 100
     * <p>
     * getSections() will return new ConfigSection, that contains sections a1 and a2 only.
     *
     * @return
     */
    //@formatter:on
    public ConfigSection getSections() {
        return getSections(null);
    }

    /**
     * Get sections (and only sections) from provided path
     *
     * @param key - config section path, if null or empty root path will used.
     * @return
     */
    public ConfigSection getSections(String key) {
        ConfigSection sections = new ConfigSection();
        ConfigSection parent = key == null || key.isEmpty() ? this.getAll() : getSection(key);
        if (parent == null) return sections;
        parent.entrySet().forEach(e -> {
            if (e.getValue() instanceof ConfigSection)
                sections.put(e.getKey(), e.getValue());
        });
        return sections;
    }

    /**
     * Get int value of config section element
     *
     * @param key - key (inside) current section (default value equals to 0)
     * @return
     */
    public int getInt(String key) {
        return this.getInt(key, 0);
    }

    /**
     * Get int value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will returned if section element is not exists
     * @return
     */
    public int getInt(String key, int defaultValue) {
        return this.get(key, ((Number) defaultValue)).intValue();
    }

    /**
     * Check type of section element defined by key. Return true this element is Integer
     *
     * @param key
     * @return
     */
    public boolean isInt(String key) {
        Object val = get(key);
        return val instanceof Integer;
    }

    /**
     * Get long value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public long getLong(String key) {
        return this.getLong(key, 0);
    }

    /**
     * Get long value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will returned if section element is not exists
     * @return
     */
    public long getLong(String key, long defaultValue) {
        return this.get(key, ((Number) defaultValue)).longValue();
    }

    /**
     * Check type of section element defined by key. Return true this element is Long
     *
     * @param key
     * @return
     */
    public boolean isLong(String key) {
        Object val = get(key);
        return val instanceof Long;
    }

    /**
     * Get double value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public double getDouble(String key) {
        return this.getDouble(key, 0);
    }

    /**
     * Get double value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will returned if section element is not exists
     * @return
     */
    public double getDouble(String key, double defaultValue) {
        return this.get(key, ((Number) defaultValue)).doubleValue();
    }

    /**
     * Check type of section element defined by key. Return true this element is Double
     *
     * @param key
     * @return
     */
    public boolean isDouble(String key) {
        Object val = get(key);
        return val instanceof Double;
    }

    /**
     * Get String value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public String getString(String key) {
        return this.getString(key, "");
    }

    /**
     * Get String value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will returned if section element is not exists
     * @return
     */
    public String getString(String key, String defaultValue) {
        Object result = this.get(key, defaultValue);
        return String.valueOf(result);
    }

    /**
     * Check type of section element defined by key. Return true this element is String
     *
     * @param key
     * @return
     */
    public boolean isString(String key) {
        Object val = get(key);
        return val instanceof String;
    }

    /**
     * Get boolean value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public boolean getBoolean(String key) {
        return this.getBoolean(key, false);
    }

    /**
     * Get boolean value of config section element
     *
     * @param key          - key (inside) current section
     * @param defaultValue - default value that will returned if section element is not exists
     * @return
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return this.get(key, defaultValue);
    }

    /**
     * Check type of section element defined by key. Return true this element is Integer
     *
     * @param key
     * @return
     */
    public boolean isBoolean(String key) {
        Object val = get(key);
        return val instanceof Boolean;
    }

    /**
     * Get List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List getList(String key) {
        return this.getList(key, null);
    }

    /**
     * Get List value of config section element
     *
     * @param key         - key (inside) current section
     * @param defaultList - default value that will returned if section element is not exists
     * @return
     */
    public List getList(String key, List defaultList) {
        return this.get(key, defaultList);
    }

    /**
     * Check type of section element defined by key. Return true this element is List
     *
     * @param key
     * @return
     */
    public boolean isList(String key) {
        Object val = get(key);
        return val instanceof List;
    }

    /**
     * Get String List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<String> getStringList(String key) {
        List value = this.getList(key);
        if (value == null) {
            return new ArrayList<>(0);
        }
        List<String> result = new ArrayList<>();
        for (Object o : value) {
            if (o instanceof String || o instanceof Number || o instanceof Boolean || o instanceof Character) {
                result.add(String.valueOf(o));
            }
        }
        return result;
    }

    /**
     * Get Integer List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<Integer> getIntegerList(String key) {
        List<?> list = getList(key);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Integer> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Integer) {
                result.add((Integer) object);
            } else if (object instanceof String) {
                try {
                    result.add(Integer.valueOf((String) object));
                } catch (Exception ex) {
                    //ignore
                }
            } else if (object instanceof Character) {
                result.add((int) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).intValue());
            }
        }
        return result;
    }

    /**
     * Get Boolean List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<Boolean> getBooleanList(String key) {
        List<?> list = getList(key);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Boolean> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Boolean) {
                result.add((Boolean) object);
            } else if (object instanceof String) {
                if (Boolean.TRUE.toString().equals(object)) {
                    result.add(true);
                } else if (Boolean.FALSE.toString().equals(object)) {
                    result.add(false);
                }
            }
        }
        return result;
    }

    /**
     * Get Double List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<Double> getDoubleList(String key) {
        List<?> list = getList(key);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Double> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Double) {
                result.add((Double) object);
            } else if (object instanceof String) {
                try {
                    result.add(Double.valueOf((String) object));
                } catch (Exception ex) {
                    //ignore
                }
            } else if (object instanceof Character) {
                result.add((double) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).doubleValue());
            }
        }
        return result;
    }

    /**
     * Get Float List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<Float> getFloatList(String key) {
        List<?> list = getList(key);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Float> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Float) {
                result.add((Float) object);
            } else if (object instanceof String) {
                try {
                    result.add(Float.valueOf((String) object));
                } catch (Exception ex) {
                    //ignore
                }
            } else if (object instanceof Character) {
                result.add((float) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).floatValue());
            }
        }
        return result;
    }

    /**
     * Get Long List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<Long> getLongList(String key) {
        List<?> list = getList(key);
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<Long> result = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof Long) {
                result.add((Long) object);
            } else if (object instanceof String) {
                try {
                    result.add(Long.valueOf((String) object));
                } catch (Exception ex) {
                    //ignore
                }
            } else if (object instanceof Character) {
                result.add((long) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).longValue());
            }
        }
        return result;
    }

    /**
     * Get Byte List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<Byte> getByteList(String key) {
        List<?> list = getList(key);

        if (list == null) {
            return new ArrayList<>(0);
        }

        List<Byte> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Byte) {
                result.add((Byte) object);
            } else if (object instanceof String) {
                try {
                    result.add(Byte.valueOf((String) object));
                } catch (Exception ex) {
                    //ignore
                }
            } else if (object instanceof Character) {
                result.add((byte) ((Character) object).charValue());
            } else if (object instanceof Number) {
                result.add(((Number) object).byteValue());
            }
        }

        return result;
    }

    /**
     * Get Character List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<Character> getCharacterList(String key) {
        List<?> list = getList(key);

        if (list == null) {
            return new ArrayList<>(0);
        }

        List<Character> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Character) {
                result.add((Character) object);
            } else if (object instanceof String) {
                String str = (String) object;

                if (str.length() == 1) {
                    result.add(str.charAt(0));
                }
            } else if (object instanceof Number) {
                result.add((char) ((Number) object).intValue());
            }
        }

        return result;
    }

    /**
     * Get Short List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<Short> getShortList(String key) {
        List<?> list = getList(key);

        if (list == null) {
            return new ArrayList<>(0);
        }

        List<Short> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Short) {
                result.add((Short) object);
            } else if (object instanceof String) {
                try {
                    result.add(Short.valueOf((String) object));
                } catch (Exception ex) {
                    //ignore
                }
            } else if (object instanceof Character) {
                result.add((short) ((Character) object).charValue());
            } else if (object instanceof Number) {
                result.add(((Number) object).shortValue());
            }
        }

        return result;
    }

    /**
     * Get Map List value of config section element
     *
     * @param key - key (inside) current section
     * @return
     */
    public List<Map> getMapList(String key) {
        List<Map> list = getList(key);
        List<Map> result = new ArrayList<>();

        if (list == null) {
            return result;
        }

        for (Object object : list) {
            if (object instanceof Map) {
                result.add((Map) object);
            }
        }

        return result;
    }

    /**
     * Check existence of config section element
     *
     * @param key
     * @param ignoreCase
     * @return
     */
    public boolean exists(String key, boolean ignoreCase) {
        if (ignoreCase) key = key.toLowerCase();
        for (String existKey : this.getKeys(true)) {
            if (ignoreCase) existKey = existKey.toLowerCase();
            if (existKey.equals(key)) return true;
        }
        return false;
    }

    /**
     * Check existence of config section element
     *
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return exists(key, false);
    }

    /**
     * Remove config section element
     *
     * @param key
     */
    public void remove(String key) {
        if (key == null || key.isEmpty()) return;
        if (super.containsKey(key)) super.remove(key);
        else if (this.containsKey(".")) {
            String[] keys = key.split("\\.", 2);
            if (super.get(keys[0]) instanceof ConfigSection) {
                ConfigSection section = (ConfigSection) super.get(keys[0]);
                section.remove(keys[1]);
            }
        }
    }

    /**
     * Get all keys
     *
     * @param child - true = include child keys
     * @return
     */
    public Set<String> getKeys(boolean child) {
        Set<String> keys = new LinkedHashSet<>();
        this.entrySet().forEach(entry -> {
            keys.add(entry.getKey());
            if (entry.getValue() instanceof ConfigSection && child) {
                ((ConfigSection) entry.getValue()).getKeys(true).forEach(childKey -> keys.add(entry.getKey() + "." + childKey));
            }
        });
        return keys;
    }

    /**
     * Get all keys
     *
     * @return
     */
    public Set<String> getKeys() {
        return this.getKeys(true);
    }
}

class FileUtils {

    public static void writeFile(String fileName, String content) throws IOException {
        writeFile(fileName, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    public static void writeFile(String fileName, InputStream content) throws IOException {
        writeFile(new File(fileName), content);
    }

    public static void writeFile(File file, String content) throws IOException {
        writeFile(file, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    public static void writeFile(File file, InputStream content) throws IOException {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream stream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = content.read(buffer)) != -1) {
            stream.write(buffer, 0, length);
        }
        stream.close();
        content.close();
    }

    public static String readFile(File file) throws IOException {
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException();
        }
        return readFile(new FileInputStream(file));
    }

    public static String readFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException();
        }
        return readFile(new FileInputStream(file));
    }

    public static String readFile(InputStream inputStream) throws IOException {
        return readFile(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private static String readFile(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String temp;
        StringBuilder stringBuilder = new StringBuilder();
        temp = br.readLine();
        while (temp != null) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(temp);
            temp = br.readLine();
        }
        br.close();
        reader.close();
        return stringBuilder.toString();
    }
}
