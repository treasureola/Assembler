# Cache System Design

## Overview

The Basic Machine Simulator implements a simple cache system that sits between the CPU and main memory. The cache improves performance by storing frequently accessed memory locations in faster storage.

## Cache Specifications

### Architecture
- **Type**: Fully Associative, Unified Cache
  - Fully Associative: Any memory block can be stored in any cache line
  - Unified: Stores both instructions and data (no separate I-cache/D-cache)
- **Size**: 16 cache lines
- **Line Size**: 1 word (16 bits)
- **Total Capacity**: 16 words

### Cache Line Structure

Each cache line contains:

```
+-------+------+--------+------------+
| Valid | Tag  |  Data  | FIFO Index |
+-------+------+--------+------------+
  1 bit  12bits  16bits    counter
```

**Fields:**
- **Valid Bit**: Indicates if the cache line contains valid data (0 = invalid, 1 = valid)
- **Tag**: Memory address of the cached word (12 bits for 2048-word address space)
- **Data**: The actual 16-bit word stored from memory
- **FIFO Index**: Counter used for FIFO replacement policy

## Replacement Policy

### FIFO (First-In, First-Out)

The cache uses a simple FIFO replacement algorithm:

1. **Cache Not Full**: 
   - New data is placed in the first invalid (empty) cache line
   - Valid bit is set to 1
   - FIFO counter is incremented

2. **Cache Full**:
   - Find the cache line with the smallest FIFO index (oldest entry)
   - Replace that line with new data
   - Update FIFO index to current counter value
   - Increment FIFO counter

**Advantages of FIFO:**
- Simple to implement
- Low overhead (just one counter)
- Predictable behavior

**Disadvantages:**
- Doesn't consider access frequency
- May evict frequently used data

## Cache Policies

### Write Policy: Write-Through

When the CPU writes to memory:
1. Write data to the cache (if address is in cache)
2. **Immediately** write data to main memory
3. If address is not in cache, allocate a new cache line (write-allocate)

**Advantages:**
- Memory always has the most recent data
- Simpler implementation
- No need for dirty bits

**Disadvantages:**
- Higher memory traffic
- Slower writes

### Read Policy: Read-Allocate

When the CPU reads from memory:
1. Check if address is in cache
2. **Cache Hit**: Return data from cache (fast)
3. **Cache Miss**: 
   - Fetch data from main memory (slow)
   - Allocate cache line using FIFO
   - Store data in cache for future access

## Cache Operation Flow

### Read Operation

```
1. CPU requests READ from address X
2. Cache checks all 16 lines for matching tag
3. IF found (HIT):
     a. Return data from cache line
     b. Increment hit counter
   ELSE (MISS):
     a. Fetch data from main memory
     b. Find victim line using FIFO
     c. Store data in cache line
     d. Update tag and valid bit
     e. Increment miss counter
4. Return data to CPU
```

### Write Operation

```
1. CPU requests WRITE to address X with data D
2. Write data D to main memory (write-through)
3. Cache checks all 16 lines for matching tag
4. IF found (HIT):
     a. Update data in cache line
   ELSE (MISS):
     a. Find victim line using FIFO
     b. Store data D in cache line
     c. Update tag and valid bit
5. Operation complete
```

## Cache Statistics

The cache tracks the following statistics:

| Metric | Description |
|--------|-------------|
| **Hits** | Number of times requested data was found in cache |
| **Misses** | Number of times requested data was NOT in cache |
| **Reads** | Total number of read operations |
| **Writes** | Total number of write operations |
| **Hit Rate** | Percentage of accesses that resulted in hits: `(Hits / (Hits + Misses)) × 100%` |

### Performance Metrics

**Good Cache Performance:**
- High hit rate (>80%)
- Programs with good locality of reference
- Sequential access patterns

**Poor Cache Performance:**
- Low hit rate (<30%)
- Random access patterns
- Access to more than 16 unique addresses without repetition

## Cache Tracing

The cache supports trace logging for debugging and analysis:

### Enabling Trace

```java
memory.enableCacheTracing("cache_trace.log");
```

### Trace Format

```
=== Cache Trace Started at 2025-10-29 00:00:00 ===
Cache Configuration: Size=16 lines, Policy=FIFO, Type=Unified
READ  MISS | Addr=0144 | Data=010421 | Insert Line=0
READ  HIT  | Addr=0144 | Data=010421 | Line=0
WRITE MISS | Addr=0310 | Data=001234 | Insert Line=1
READ  MISS | Addr=0400 | Data=000000 | Evict Line=0 (Tag=0144)
=== Cache Trace Ended at 2025-10-29 00:01:00 ===
```

**Trace Entry Types:**
- `READ HIT`: Data found in cache
- `READ MISS`: Data not in cache, fetched from memory
- `WRITE HIT`: Write to existing cache entry
- `WRITE MISS`: Write to new cache entry
- `Evict Line`: Cache line was replaced (with old tag shown)

## Cache Control API

### Memory Class Methods

```java
// Enable/Disable cache
memory.enableCache();
memory.disableCache();
boolean enabled = memory.isCacheEnabled();

// Cache tracing
memory.enableCacheTracing("filename.log");
memory.disableCacheTracing();

// Statistics
String stats = memory.getCacheStatistics();
String contents = memory.getCacheContents();
memory.resetCacheStatistics();

// Direct access (bypassing cache)
int value = memory.readWordDirect(address);
memory.writeWordDirect(address, value);
```

### Cache Class Methods

```java
Cache cache = memory.getCache();

// Access cache
int data = cache.read(address, memory);
cache.write(address, data, memory);

// Control
cache.reset();              // Clear all cache lines
cache.resetStatistics();    // Reset counters only

// Query
int hits = cache.getHits();
int misses = cache.getMisses();
double hitRate = cache.getHitRate();
boolean inCache = cache.isInCache(address);
```

## Cache Performance Analysis

### Test Case 1: Sequential Access

```assembly
LDR 0,0,100
LDR 0,0,101
LDR 0,0,102
LDR 0,0,103
```

**Expected:** 4 misses (first access to each address)

### Test Case 2: Repeated Access

```assembly
LDR 0,0,100
LDR 0,0,100
LDR 0,0,100
```

**Expected:** 1 miss, 2 hits (80% hit rate)

### Test Case 3: FIFO Eviction

```assembly
; Fill cache with 16 unique addresses
LDR 0,0,0
LDR 0,0,100
...
LDR 0,0,1500  ; 16th address

; Add 17th address
LDR 0,0,1600  ; Evicts address 0 (oldest)

; Try to access evicted address
LDR 0,0,0     ; MISS (was evicted)
```

**Expected:** 17 misses for new addresses, then miss for evicted address

## Integration with Memory Hierarchy

```
┌─────────────────┐
│      CPU        │
│   (Registers)   │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│   Cache (16)    │  ← Fast access
│   FIFO Policy   │     (~1 cycle)
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  Main Memory    │  ← Slow access
│  (2048 words)   │     (~10 cycles)
└─────────────────┘
```

## Design Decisions

### Why Fully Associative?

**Pros:**
- No conflict misses
- Flexible placement
- Simple tag comparison

**Cons:**
- More complex hardware (all lines must be checked)
- Slower for large caches

With only 16 lines, the complexity is manageable.

### Why FIFO?

**Alternatives considered:**
- **LRU (Least Recently Used)**: More complex, needs access counters for each line
- **Random**: Unpredictable behavior
- **LFU (Least Frequently Used)**: Very complex, needs frequency counters

FIFO provides a good balance of simplicity and performance for this educational simulator.

### Why Write-Through?

**Alternatives considered:**
- **Write-Back**: Better performance but requires dirty bits and more complex write logic

Write-through simplifies the design and ensures data consistency at all times, which is important for debugging and trace analysis.

## Cache Demonstration Programs

See `tests/test_cache_demo.src` for comprehensive cache test cases.

Run with:
```bash
# Enable cache tracing
memory.enableCacheTracing("cache_trace.log");

# Run program
cpu.run();

# Check statistics
System.out.println(memory.getCacheStatistics());
System.out.println(memory.getCacheContents());
```

## Future Enhancements

Possible improvements for Phase II:

1. **Larger Cache**: Increase to 32 or 64 lines
2. **Set-Associative**: Implement 2-way or 4-way set associative cache
3. **LRU Replacement**: Use LRU instead of FIFO
4. **Write-Back Policy**: Implement write-back with dirty bits
5. **Multi-Level Cache**: Add L1 and L2 cache hierarchy
6. **Cache Coherency**: For multi-core simulations

## References

- Computer Organization and Design (Patterson & Hennessy)
- CS6461 Lecture Notes on Cache Design
- "Cache Memory Design Considerations" (IEEE)

