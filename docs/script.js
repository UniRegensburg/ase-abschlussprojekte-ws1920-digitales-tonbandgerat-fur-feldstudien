const $targetArea = $('.mockup-section')
const $heroImage = $('#hero-image')
var xAngle = 0
var yAngle = 0
var z = 50

$targetArea.on('mousemove', (e) => {
    var xRel = e.pageX - $heroImage.offset().left
    var yRel = e.pageY - $heroImage.offset().top
    var width = $heroImage.width()

    xAngle = (0.5 - (yRel / width)) * 10
    yAngle = -(0.5 - (xRel / width)) * 10

    updateView()
})

$targetArea.on('mouseleave', (e) => {
    $heroImage.css({"transform":"perspective(525px) translateZ(0) rotateX(0deg) rotateY(0deg)","transition":"all 150ms linear 0s","-webkit-transition":"all 150ms linear 0s"})
})

updateView = () => {
    $heroImage.css({"transform":"perspective(525px) translateZ(" + z + "px) rotateX(" + xAngle + "deg) rotateY(" + yAngle + "deg)","transition":"none","-webkit-transition":"none"})
}