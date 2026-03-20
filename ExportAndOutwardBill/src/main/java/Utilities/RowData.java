package Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RowData {

	 private final Map<String, String> headerMap;
	    private final List<String> indexList;

	    public RowData(String[] headers, String[] rowData) {
	        this.headerMap = new HashMap<>();
	        this.indexList = new ArrayList<>();

	        for (int i = 0; i < rowData.length; i++) {
	            headerMap.put(headers[i], rowData[i]);
	            indexList.add(rowData[i]);
	        }
	    }

	    public String getByHeader(String header) {
	        return headerMap.get(header);
	    }

	    public String getByIndex(int index) {
	        return indexList.get(index);
	    }

	    public Map<String, String> getHeaderMap() {
	        return headerMap;
	    }

	    public List<String> getIndexList() {
	        return indexList;
	    }

	    @Override
	    public String toString() {
	        return "RowData{" +
	                "headerMap=" + headerMap +
	                ", indexList=" + indexList +
	                '}';
	    }
	   
}

