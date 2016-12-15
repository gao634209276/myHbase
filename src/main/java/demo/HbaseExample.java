package demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 不需要实现数据库连接池，
 * 内置MAVEN依赖错误，使用另外的客户端包hbase-shaded-client
 */
public class HbaseExample {

	/**
	 * 不强制性创建表
	 *
	 * @param tableName 表名
	 * @param family    列族列表
	 * @param config    配置信息
	 * @throws Exception
	 */
	public static void creatTable(String tableName, String[] family, Configuration config) throws Exception {
		Connection connection = ConnectionFactory.createConnection(config);
		Admin admin = connection.getAdmin();
		HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(tableName));
		for (int i = 0; i < family.length; i++) {
			desc.addFamily(new HColumnDescriptor(family[i]));
		}
		if (admin.tableExists(desc.getTableName())) {
			System.out.println("table Exists!");
			throw new Exception("table Exists!");
		} else {
			admin.createTable(desc);
			System.out.println("create table Success!");
		}
	}


	/**
	 * 强制性创建表
	 *
	 * @param tableName 表名
	 * @param family    列族列表
	 * @param config    配置信息
	 * @throws Exception
	 */
	public static void creatTableForce(String tableName, String[] family, Configuration config) throws Exception {
		Connection connection = ConnectionFactory.createConnection(config);
		Admin admin = connection.getAdmin();
		HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(tableName));
		for (int i = 0; i < family.length; i++) {
			desc.addFamily(new HColumnDescriptor(family[i]));
		}
		if (admin.tableExists(desc.getTableName())) {
			admin.disableTable(desc.getTableName());
			admin.deleteTable(desc.getTableName());
		}
		admin.createTable(desc);
		System.out.println("create table Success!");


	}

	/**
	 * 删表
	 *
	 * @param tableName 表名
	 * @param config    配置信息
	 * @throws Exception
	 */
	public static void deleteTable(String tableName, Configuration config) throws Exception {
		Connection connection = ConnectionFactory.createConnection(config);
		Admin admin = connection.getAdmin();
		TableName tn = TableName.valueOf(tableName);
		if (admin.tableExists(tn)) {
			admin.disableTable(tn);
			admin.deleteTable(tn);
		}

	}


	/**
	 * 查看已有表
	 *
	 * @param config 配置信息
	 * @return 返回表描述信息
	 * @throws IOException
	 */
	public static HTableDescriptor[] listTables(Configuration config) throws IOException {
		Connection connection = ConnectionFactory.createConnection(config);
		Admin admin = connection.getAdmin();
		HTableDescriptor hTableDescriptors[] = admin.listTables();
		return hTableDescriptors;

	}


	/**
	 * 插入数据
	 *
	 * @param tableName 表名
	 * @param config    配置信息
	 * @param rowkey    行key
	 * @param colFamily 列族
	 * @param col       子列
	 * @param val       值
	 * @throws Exception
	 */
	public static void instertRow(String tableName, Configuration config, String rowkey, String colFamily, String col, String val) throws Exception {
		Connection connection = ConnectionFactory.createConnection(config);
		Table table = connection.getTable(TableName.valueOf(tableName));
		Put put = new Put(Bytes.toBytes(rowkey));
		put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col), Bytes.toBytes(val));
		table.put(put);

		//批量插入
		   /* List<Put> putList = new ArrayList<Put>();
			puts.add(put);
            table.put(putList);*/
		table.close();
		System.out.printf("adding success!!Table:%s,Row:%s,Column=%s:%s,Value=%s\n", tableName, rowkey, colFamily, col, val);
	}


	/**
	 * 删除数据
	 *
	 * @param tableName 表名
	 * @param config    配置信息
	 * @param rowkey    行key
	 * @param colFamily 列族
	 * @param col       子列
	 * @throws Exception
	 */
	public static void deleRow(String tableName, Configuration config, String rowkey, String colFamily, String col) throws Exception {
		Connection connection = ConnectionFactory.createConnection(config);
		Table table = connection.getTable(TableName.valueOf(tableName));
		Delete delete = new Delete(Bytes.toBytes(rowkey));
		//删除指定列族
		if (colFamily != null && col == null)
			delete.addFamily(Bytes.toBytes(colFamily));
		//删除指定列
		if (colFamily != null && col != null)
			delete.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col));
		table.delete(delete);
		//批量删除
		   /* List<Delete> deleteList = new ArrayList<Delete>();
			deleteList.add(delete);
            table.delete(deleteList);*/
		table.close();
	}

	public static void deleRow(String tableName, Configuration config, String rowkey, String colFamily) throws Exception {
		deleRow(tableName, config, rowkey, colFamily, null);
	}

	public static void deleRow(String tableName, Configuration config, String rowkey) throws Exception {
		deleRow(tableName, config, rowkey, null, null);
	}


	/**
	 * 根据rowkey查找数据
	 *
	 * @param tableName 表名
	 * @param config    配置信息
	 * @param rowkey    行key
	 * @param colFamily 列族
	 * @param col       子列
	 * @return
	 * @throws Exception
	 */
	public static Result getData(String tableName, Configuration config, String rowkey, String colFamily, String col) throws Exception {
		Connection connection = ConnectionFactory.createConnection(config);
		Table table = connection.getTable(TableName.valueOf(tableName));
		Get get = new Get(Bytes.toBytes(rowkey));
		if (colFamily != null && col == null)
			get.addFamily(Bytes.toBytes(colFamily));
		if (colFamily != null && col != null)
			get.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col));
		Result result = table.get(get);
		table.close();
		return result;
	}

	public static Result getData(String tableName, Configuration config, String rowkey, String colFamily) throws Exception {
		return getData(tableName, config, rowkey, colFamily, null);
	}

	public static Result getData(String tableName, Configuration config, String rowkey) throws Exception {
		return getData(tableName, config, rowkey, null, null);
	}


	/**
	 * 批量查找数据
	 *
	 * @param tableName 表名
	 * @param config    配置文件
	 * @param startRow  开始的行key
	 * @param stopRow   停止的行key
	 * @param limit     限制
	 * @return 返回结果
	 * <p>
	 * hbase会将自己的元素按照key的ASCII码排序
	 * <p>
	 * 找出5193开头的元素
	 * <p>
	 * 5193:1
	 * 5193:2
	 * 5194:1
	 * 51939:1
	 * 51942:1
	 * <p>
	 * scan.setStartRow("5193:#");
	 * scan.setStopRow("5193::");
	 * <p>
	 * 原因：ASCII排序中："#" < "0-9" < ":"
	 * 取出来的将是5193:后面跟着数字的元素
	 */
	public static List<Result> scanData(String tableName, Configuration config, String startRow, String stopRow, int limit) throws Exception {
		Connection connection = ConnectionFactory.createConnection(config);
		Table table = connection.getTable(TableName.valueOf(tableName));
		Scan scan = new Scan();
		if (startRow != null && stopRow != null) {
			scan.setStartRow(Bytes.toBytes(startRow));
			scan.setStopRow(Bytes.toBytes(stopRow));
		}
		scan.setBatch(limit);
		List<Result> result = new ArrayList<Result>();
		ResultScanner resultScanner = table.getScanner(scan);
		for (Result r : resultScanner) {
			result.add(r);
		}
		table.close();
		return result;
	}

	public static List<Result> scanData(String tableName, Configuration config, int limit) throws Exception {
		return scanData(tableName, config, null, null, limit);
	}


	/**
	 * 打印表
	 *
	 * @param tables 打印的表描述对象
	 */
	public static void printTables(HTableDescriptor[] tables) {
		for (HTableDescriptor t : tables) {
			HColumnDescriptor[] columns = t.getColumnFamilies();
			System.out.printf("tables:%s,columns-family:\n", t.getTableName());
			for (HColumnDescriptor column : columns) {
				System.out.printf("\t%s\n", column.getNameAsString());
			}
		}
	}


	/**
	 * 格式化输出
	 *
	 * @param result 结果
	 */
	public static void showCell(Result result) {
		Cell[] cells = result.rawCells();
		for (Cell cell : cells) {
			System.out.println("RowName:" + new String(CellUtil.cloneRow(cell)) + " ");
			System.out.println("Timetamp:" + cell.getTimestamp() + " ");
			System.out.println("column Family:" + new String(CellUtil.cloneFamily(cell)) + " ");
			System.out.println("row Name:" + new String(CellUtil.cloneQualifier(cell)) + " ");
			System.out.println("value:" + new String(CellUtil.cloneValue(cell)) + " ");
			System.out.println("---------------");
		}
	}

	public static void main(String... args) {
		Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.property.clientPort", "2181");
		config.set("hbase.zookeeper.quorum", "192.168.11.73");
		config.set("hbase.master", "192.168.11.73:60000");
		String tablename = "visitor";
		String[] column_family = {"value"};
		try {
			//创建表
			//creatTableForce(tablename, column_family, config);

			//列出表信息
			HTableDescriptor[] tables = listTables(config);
			printTables(tables);

			//插入行
			for (int i = 1; i < 5; i++)
				instertRow(tablename, config, "row1", column_family[0], i + "", "value");

			//获取单行值
			Result result = getData(tablename, config, "row1", column_family[0]);
			showCell(result);

			//扫描表，获取前20行
			List<Result> results = scanData(tablename, config, 20);
			for (Result r : results) {
				showCell(r);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
