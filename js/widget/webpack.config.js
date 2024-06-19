const path = require('path');
 
module.exports = {
  entry: './src/widget.js',
  resolve: {
    fallback: {
      "stream": require.resolve("stream-browserify"),
      "crypto": require.resolve("crypto-browserify"),
      "vm": require.resolve("vm-browserify")
    }
  },
  performance: {
    hints: false,
    maxEntrypointSize: 512000,
    maxAssetSize: 512000
  },
  output: {
    filename: 'hyperIdWidget.js',
    path: path.resolve(__dirname, 'dist'),
  },
};