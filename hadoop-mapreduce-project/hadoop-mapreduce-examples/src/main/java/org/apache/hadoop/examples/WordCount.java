/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.examples;

import java.io.IOException;
import java.util.StringTokenizer;

// You will typically import these classes every mapreduce job you write.
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

// MapReduce program genrally consist of three portions: The Mapper, the reducer and the driver codee
public class WordCount {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    // The Mapper implementation, via the map method, processes one line at a time, as provided by the
	// specified TextInputFormat. It then splits the line into tokens separated by whitespaces,
	// via the StringTokenizer, and emits a key-value pair of < <word>, 1>.
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, one);
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();
    // The Reducer implementation, via the reduce method just sums up the values,
    // which are the occurence counts for each key (i.e. words in this example).
    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  // Usually configure your mapreduce job in the main method.
  public static void main(String[] args) throws Exception {
        // To configure your mapreduce job, create a new Configuration object. some use the Hadoop default values
    // some options will read from the Hadoop configuration files, e.g. /etc/hadoop/conf
    Configuration conf = new Configuration();

    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length < 2) {
      System.err.println("Usage: wordcount <in> [<in>...] <out>");
      System.exit(2);
    }

    // Give the mapreduce job a meaningful name.
    Job job = Job.getInstance(conf, "word count");
    //specicify the class which will be called to run the mapreduce job.
    job.setJarByClass(WordCount.class);
    // Give the job about which class are be instatiated as the mapper and reducer.
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    // specify the input directory from which data will be read.
    // The data passed to the mapper is specified by an inputformat, in this case, it is FileInputFormat
	// The InputFormat determines how to split the input data into input splites. Each mapper will deal
	// with a single input split.InputFormat is a factory for RecordReader objects to extract (key,value)
    // records from the input source. output directory to which the final output will be written.
    for (int i = 0; i < otherArgs.length - 1; ++i) {
      FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
    }
    FileOutputFormat.setOutputPath(job,
      new Path(otherArgs[otherArgs.length - 1]));
    // run the job.
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
