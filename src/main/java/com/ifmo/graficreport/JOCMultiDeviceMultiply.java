package com.ifmo.graficreport;

import static org.jocl.CL.*;

import org.jocl.*;

public class JOCMultiDeviceMultiply {
    /**
     * The source code of the OpenCL program to execute
     */
    private static final String MULTIPLY_TWO_VECTORS_PROGRAM =
            "__kernel void " +
                    "sampleKernel(__global const float *a," +
                    "             __global const float *b," +
                    "             __global float *c)" +
                    "{" +
                    "    int gid = get_global_id(0);" +
                    "    c[gid] = a[gid] * b[gid];" +
                    "}";


    /**
     * The entry point of this sample
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        profileCalculating(0);
        profileCalculating(1);
        profileCalculating(2);
    }

    private static void profileCalculating(int deviceIndex) {
        // The platform, device type and device number
        // that will be used
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int platformIndex = 0;

        // Obtain the number of platforms
        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Obtain the number of devices for the platform
        int[] numDevicesArray = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id[] devices = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        String deviceName = getString(device, CL_DEVICE_NAME);
        System.out.println("DEVICE: " + deviceName);

        calculateNTimes(10, platform, device);
        calculateNTimes(100, platform, device);
        calculateNTimes(200, platform, device);
        calculateNTimes(300, platform, device);
    }

    private static void calculateNTimes(int times, cl_platform_id platform, cl_device_id device) {
        final long before = System.nanoTime();
        for (int i = 0; i < times; i++) {
            calculate(platform, device);
        }
        final long after = System.nanoTime();
        final float totalDurationMs = (after - before) / 1e6f;
        System.out.println(String.format("Elapsed time for %s times: %s", times, totalDurationMs));
    }

    private static void calculate(cl_platform_id platform, cl_device_id device) {
        // Create input- and output data
        final int n = 10_000_000;
        final float[] srcArrayA = new float[n];
        final float[] srcArrayB = new float[n];
        final float[] dstArray = new float[n];
        for (int i = 0; i < n; i++) {
            srcArrayA[i] = i;
            srcArrayB[i] = i;
        }
        Pointer srcA = Pointer.to(srcArrayA);
        Pointer srcB = Pointer.to(srcArrayB);
        Pointer dst = Pointer.to(dstArray);

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Create a context for the selected device
        cl_context context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        cl_command_queue commandQueue =
                clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        cl_mem[] memObjects = new cl_mem[3];
        memObjects[0] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, srcA, null);
        memObjects[1] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, srcB, null);
        memObjects[2] = clCreateBuffer(context,
                CL_MEM_READ_WRITE,
                Sizeof.cl_float * n, null, null);

        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context,
                1, new String[]{MULTIPLY_TWO_VECTORS_PROGRAM}, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "sampleKernel", null);

        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));

        // Set the work-item dimensions
        long[] global_work_size = new long[]{n};
        long[] local_work_size = new long[]{1};

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
                n * Sizeof.cl_float, dst, 0, null, null);

        // Release kernel, program, and memory objects
        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseMemObject(memObjects[2]);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device    The device
     * @param paramName The parameter name
     * @return The value
     */
    private static String getString(cl_device_id device, int paramName) {
        long[] size = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);
        byte[] buffer = new byte[(int) size[0]];
        clGetDeviceInfo(device, paramName,
                buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length - 1);
    }

}
