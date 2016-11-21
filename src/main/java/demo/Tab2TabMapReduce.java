package demo;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * HADOOP_CLASSPATH=`${HBASE_HOME}/bin/hbase classpath`
 */
public class Tab2TabMapReduce extends Configured implements Tool {
	//mapper class,读取数据
	public static class TabMapper extends TableMapper<Text, Put> {
		private Text rowkey = new Text();

		@Override
		protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
			byte[] bytes = key.get();
			rowkey.set(Bytes.toString(bytes));

			Put put = new Put(bytes);
			for (Cell cell : value.rawCells()) {
				//add cell
				if ("info".equals(CellUtil.cloneFamily(cell))) {
					if ("name".equals(CellUtil.cloneQualifier(cell))) {
						put.add(cell);
					}
				}
			}
		}
	}

	// reduce class
	public static class TabReduce extends TableReducer<Text, Put, ImmutableBytesWritable> {
		@Override
		protected void reduce(Text key, Iterable<Put> values, Context context) throws IOException, InterruptedException {
			for (Put put : values) {
				context.write(null, put);
			}
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		//create job
		Job job = Job.getInstance(getConf(), this.getClass().getSimpleName());
		//set run class
		job.setJarByClass(this.getClass());

		//set Mapper
		Scan scan = new Scan();
		scan.setCaching(500);
		scan.setCacheBlocks(false);
		TableMapReduceUtil.initTableMapperJob(
				"tab1",//input table
				scan,//scan input
				TabMapper.class,//set mapper class
				Text.class,//mapper output key
				Put.class,//mapper output value
				job //set job
		);

		TableMapReduceUtil.initTableReducerJob(
				"tab2",//output table
				TableReducer.class,//set reduce class
				job //set job
		);
		job.setNumReduceTasks(1);
		boolean b = job.waitForCompletion(true);
		if (!b) {
			System.err.println("error with job !!!");
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {

		//create config
		Configuration conf = HBaseConfiguration.create();

		//submit job
		int status = ToolRunner.run(conf, new Tab2TabMapReduce(), args);

		//exit
		System.exit(status);
	}
}
