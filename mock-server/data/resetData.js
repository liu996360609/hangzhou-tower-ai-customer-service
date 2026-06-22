/**
 * 数据重置函数 - 深克隆初始数据
 */
const { getInitialData } = require('./initialData');

/**
 * 深克隆数据（返回全新的数据副本）
 */
function deepCloneData() {
  return JSON.parse(JSON.stringify(getInitialData()));
}

/**
 * 将数据存储重置为初始状态
 * @param {Object} dataStore - 当前的数据存储对象
 */
function resetData(dataStore) {
  const initial = getInitialData();
  // 清空现有数据
  Object.keys(dataStore).forEach(key => delete dataStore[key]);
  // 深克隆初始数据到数据存储
  Object.assign(dataStore, deepCloneData());
  console.log('[Reset] 数据已重置为初始状态');
}

module.exports = { resetData, deepCloneData };
