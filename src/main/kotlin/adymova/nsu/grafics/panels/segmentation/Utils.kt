package adymova.nsu.grafics.panels.segmentation

fun NormalizedCutSegmentation.apply(bufferedImage: Image) = MeanShiftSegmentation()
    .apply(bufferedImage, 0.01, 15.0001f)