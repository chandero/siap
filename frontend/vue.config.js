module.exports = {
  devServer: {
    proxy: {
      '^/api': {
        target: 'http://localhost:9091',
        secure: false,
        changeOrigin: true,
        onProxyReq: (proxyReq, req, res) => req.setTimeout(60 * 60 * 1000)
      },
      '^/ipa': {
        target: 'http://localhost:3000',
        pathRewrite: {
          '^/ipa': '/'
        },
        secure: false,
        changeOrigin: true,
        onProxyReq: (proxyReq, req, res) => req.setTimeout(60 * 60 * 1000)
      }
    },
    host: '127.0.0.1',
    port: 81, // CHANGE YOUR PORT HERE!
    https: false,
    hotOnly: false
  },
  configureWebpack: {
    optimization: {
      splitChunks: {
        minSize: 10000,
        maxSize: 250000
      }
    },
    watch: true,
    watchOptions: {
      aggregateTimeout: 300,
      poll: 1000
    }
  }
}
