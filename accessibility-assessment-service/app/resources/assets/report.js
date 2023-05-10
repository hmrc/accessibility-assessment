const q = function() {
  return [].concat(...document.querySelectorAll.apply(document, arguments))
}

document.querySelector('body').classList.add('js-enabled')

;
(function() {
  const hiddenTabClass = 'hidden-waiting-for-tab-activation'
  const allAreasToControl = []
  const allTabs = []
  let isFirstTab = true
  q('.tabs .tab').forEach(tab => {
    const link = tab.querySelector('a')
    if (!link) {
      console.error('Can\'t initialise a tab with no link')
      return;
    }
    allTabs.push(tab)
    const selectorForAreaToControl = '#' + link.getAttribute('href').split('#').join('')
    if (isFirstTab) {
      tab.classList.add('tab--active')
    }
    q(selectorForAreaToControl).forEach(areaToControl => {
      allAreasToControl.push(areaToControl)
      if (!isFirstTab) {
        areaToControl.classList.add(hiddenTabClass)
      }
    })
    link.addEventListener('click', (e) => {
      allAreasToControl.forEach(areaToControl => {
        areaToControl.classList.add(hiddenTabClass)
      })
      allTabs.forEach(tab => {
        tab.classList.remove('tab--active')
      })
      tab.classList.add('tab--active')
      q(selectorForAreaToControl).forEach(areaToControl => {
        areaToControl.classList.remove(hiddenTabClass)
      })
      e.preventDefault()
    })
    isFirstTab = false
  })

})();
