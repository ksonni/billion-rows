# One billion rows

This is based on the [one billion rows challenge](https://github.com/gunnarmorling/1brc), 
which is a project to find the quickest way to process a billion
row flat-file using modern Java.

This project however is a simplified exploration of using different Java concurrency APIs 
to solve the problem without delving into low-level optimisations/unsafe to keep the code readable, mainly:
- Thread pool executors
- Virtual threads
- Fork/Join framework

## Problem 
A text file with 1 billion rows of weather data in the following format is provided:
<pre>
Bathgate;-26.8
Kerman;44.1
Gorontalo;-79.1
</pre>
Following info needs to be calculated:
- City with the lowest reading
- City with the highest reading
- Mean temperature reading across all stations

## Results
On a controlled 8-core M1 mac with just the terminal running, following average times 
were observed for different implementations across multiple runs:

### OpenJDK 24

| Class                         | Strategy                         | Time Taken |
|:------------------------------|:---------------------------------|-----------:|
| SerialSummaryBuilder          | Single threaded line by line     |     166.3s |
| PlatformThreadsSummaryBuilder | Dedicated thread pool            |      32.4s |
| VirtualThreadsSummaryBuilder  | Java 21 virtual threads          |      30.8s |
| ForkJoinSummaryBuilder        | Using ForkJoin API RecursiveTask |      30.6s |

Perhaps more efficiencies can be found by using float-parsing tricks/unsafe APIs etc., but these were  avoided for simplicity.

## Algorithm 
Processing the file line by line and computing the mean doesn't fully use the multicore capabilities of the hardware. It can be solved concurrently as follows:

### Partitioning
- First step is to delimit fixed-size (roughly 32MB) partitions that can be processed independently *without reading the whole file*.
- This means that each partition must contain entire "rows" of data
- This can be achieved with file-seeking using `RandomAccessFile` in Java
  - Start the file pointer at 0
  - Jump the pointer 32 * 1024 * 1024 positions (32MB)
  - Progress the pointer till the next newline character is seen
  - Record the start and end positions as a "partition"
  - Repeat this process till the end of the file is reached

### Processing partitions
- Pass partition info and a reference to the file to different concurrent execution units
- Execution units here can be fork-join/thread pool/virtual threads etc.
- Access to the file is linear but parsing the data is concurrent
  - Through experimentation, it was found that reading the file concurrently doesn't perform as well
  - The CPU-bound bottleneck tends to be splitting blobs/float parsing etc. rather than I/O
- Calculate the min, max, number of entries & sum for each partition

### Merging
- Finally, all the totals are merged and the mean is calculated for the file as whole

## Testing
- Tests to verify results of the concurrent algorithm against the simple serial one exist
- Tests on a smaller 100,000 line file can be run with `./gradlew test` (Needs JDK 21+)
- The script provided by the [original project](https://github.com/gunnarmorling/1brc/blob/main/src/main/python/create_measurements.py) can be used to generate a billion row file for performance runs

