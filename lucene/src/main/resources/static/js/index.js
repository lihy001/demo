$(document).ready(function(){
$(".searchSubtn").click(function(){
var word=$.trim($("#wordInput").val());
if(word.length>0){
search(word);
};
});
$("#wordInput").on("keyup",function(e){
	var word=$.trim($("#wordInput").val());
  if(e.keyCode ==13&&word.length>0){
	search(word);
  }

});

});

function  search(wd){

$.ajax({
url:"search",
type:"post",
data:{wd:wd},
success:function(d){
showDoc(d);
}
});
}

function  showDoc(dataList){
$("#totalNum").text(dataList.length);
var $docTemp=$(".templates .doc");
var $contentR=$(".content-r");
//移除节点
$contentR.find(".doc").remove();
for(var doc of dataList.docList){
var $docClone=$docTemp.clone();
$docClone.find(".docTitle").html(doc.docName);
$docClone.find(".docContent").html(doc.docAbstract);
$docClone.find(".docLink").html(doc.docPath);
$docClone.find(".docLink").html(doc.docPath.substr(0,20)+"...&nbsp;");
$docClone.find(".docLink").attr("href",doc.docPath);
$contentR.append($docClone);
}



}