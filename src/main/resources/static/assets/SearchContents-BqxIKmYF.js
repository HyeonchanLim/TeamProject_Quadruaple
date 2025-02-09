import{g as v,c as E,e as C,r as c,a as A,j as e,u as k,b as I}from"./index-DlYLfxzI.js";import"./TitleHeader-ChFl6bwT.js";import{a as y,S as $}from"./SearchItems-CMsCGNaf.js";import{a as N}from"./axios-upsvKRUO.js";import{a as _}from"./index-DU7ewKG7.js";import{a as B}from"./pic-Bp3CpqNF.js";import{c as L}from"./match-R6ks79gH.js";import"./jwt-7u2p2k-8.js";import{D as P}from"./DockBar-Bumwoe7Z.js";import"./iconBase-DizHl2PJ.js";import"./index-2xNAINv_.js";import"./index-DB6L5INb.js";import"./context-YIeslHxJ.js";import"./Compact-Bk0X0DCw.js";import"./BaseInput-BvEJwfAa.js";import"./button-BR1h7VSD.js";import"./ContextIsolator-C_kIobP2.js";import"./Keyframes-BOStT4yD.js";import"./CloseCircleFilled-B4CUy9UF.js";import"./useCSSVarCls-Ca5RJBqo.js";import"./index-BTlz1CL1.js";import"./index-DUVLbqhb.js";import"./index-B13msMtW.js";import"./KeyCode-DNlgD2sM.js";import"./Skeleton-Cgw2lAb-.js";import"./index-AO4cOa6f.js";import"./index-SwRgpoDr.js";import"./index-CQou9XGe.js";import"./ExclamationCircleFilled-f_0hTFyt.js";import"./InfoCircleFilled-B3ipaK9n.js";import"./CloseOutlined-B68bXNli.js";const U=({searchData:x,setSearchData:s,setSearchValue:l,searchValue:o,setSearchState:n})=>{const a=v("accessToken"),{userId:r}=E(C),[m,h]=c.useState([]),[p,g]=c.useState([]),d=A(),i=async()=>{try{const f=(await N.get("/api/search/popular")).data;h(f.data)}catch(t){console.log("인기검색어 결과",t)}};c.useEffect(()=>{},[m]);const j=async()=>{try{const f=(await N.get(`/api/search/basic?user_id=${r}`,{headers:{Authorization:`Bearer ${a}`}})).data;g(f.data)}catch(t){console.log("최근 본 검색 결과",t)}};c.useEffect(()=>{},[p]);const b=t=>{console.log("클릭한 인기 검색어:",t),l(t.strfName),d(`/contents/index?strfId=${t.strfId}`)},R=t=>{console.log(t),d(`/contents/index?strfId=${t.strfId}`)},T=async t=>{const f={strf_id:t.strfId};try{const S=await N.patch(`/api/recent/hide?strf_id=${t.strfId}`,{...f},{headers:{Authorization:`Bearer ${a}`}});console.log(S.data),S.data&&j()}catch(S){console.log("개별 삭제",S)}},w=async()=>{try{const t=await N.patch("/api/recent/hide/all",{},{headers:{Authorization:`Bearer ${a}`}});console.log(t.data),t.data&&j()}catch(t){console.log("개별 삭제",t)}};return c.useEffect(()=>{i(),a&&j()},[]),e.jsxs("div",{className:"px-[32px] flex flex-col gap-[50px]",children:[e.jsxs("div",{className:"flex flex-col gap-[30px]",children:[e.jsx("h2",{className:"text-[24px] font-semibold text-slate-700",children:"인기 검색어"}),e.jsx("ul",{className:"flex gap-[20px] flex-wrap",children:m?m==null?void 0:m.map((t,f)=>e.jsx("li",{className:"cursor-pointer text-slate-700 bg-slate-50 px-[20px] py-[10px] rounded-[20px]",onClick:()=>b(t),children:t.strfName},f)):e.jsx("li",{className:"text-slate-700 bg-slate-50 px-[20px] py-[10px] rounded-[20px]",children:"데이터 없음"})})]}),a?e.jsxs("div",{className:"flex flex-col gap-[30px]",children:[e.jsxs("div",{className:"flex justify-between items-center",children:[e.jsx("h2",{className:"text-[24px] font-semibold text-slate-700",children:"최근 본 목록"}),e.jsx("button",{type:"button",className:"text-slate-400 text-[18px]",onClick:()=>w(),children:"모두 삭제"})]}),e.jsx("ul",{className:"flex flex-col gap-[20px]",children:p?p==null?void 0:p.map((t,f)=>e.jsxs("li",{className:"flex cursor-pointer items-center justify-between",children:[e.jsxs("div",{className:"flex gap-[15px]",onClick:()=>R(t),children:[e.jsx("div",{className:"w-[80px] h-[80px] rounded-2xl overflow-hidden",children:e.jsx("img",{className:"w-full h-full object-cover",src:t.strfPic?`${B}${t.strfId}/${t.strfPic}`:"/images/logo_icon_4.png",alt:t.strfName})}),e.jsxs("div",{className:"flex flex-col gap-[5px] justify-center",children:[e.jsx("div",{className:"text-[18px] text-slate-700 font-semibold",children:t.strfName}),e.jsxs("div",{className:"flex gap-[5px]",children:[e.jsx("span",{className:"text-slate-500 text-[14px]",children:L(t.category)}),e.jsx("span",{className:"text-slate-500 text-[14px]",children:"•"}),e.jsx("span",{className:"text-slate-500 text-[14px]",children:t.locationTitle})]})]})]}),e.jsx("button",{type:"button",className:"text-slate-400 text-[20px]",onClick:()=>T(t),children:e.jsx(_,{})})]},f)):null})]}):null]})},u=[{type:"all",name:"전체"},{type:"TOUR",name:"관광지"},{type:"STAY",name:"숙소"},{type:"RESTAUR",name:"맛집"},{type:"FEST",name:"축제"}],F=({searchValue:x,searchData:s,setSearchData:l})=>{const[o,n]=c.useState(0),[a,r]=c.useState(0);c.useEffect(()=>{console.log("selectedCate",o),r(0)},[o]),c.useEffect(()=>{console.log("searchData",s)},[s]);const m=c.useRef(null),h=Array.isArray(s)?s.filter(i=>i.category==="TOUR"):[],p=Array.isArray(s)?s.filter(i=>i.category==="STAY"):[],g=Array.isArray(s)?s.filter(i=>i.category==="RESTAUR"):[],d=Array.isArray(s)?s.filter(i=>i.category==="FEST"):[];return e.jsxs("div",{className:"px-[32px] py-[30px] flex flex-col gap-[30px]",children:[e.jsx("ul",{className:"flex gap-[10px]",ref:m,children:u.map((i,j)=>e.jsx("li",{className:`cursor-pointer font-semibold text-[16px] w-[124px] flex justify-center items-center px-[15px] py-[10px] gap-[10px] rounded-[8px] ${j===o?"bg-primary text-white":"bg-white text-slate-500"}`,onClick:()=>{n(j)},children:i.name},j))}),s.length===0?e.jsx("div",{className:"flex justify-center items-center h-[500px]",children:e.jsx("p",{className:"text-[20px] font-semibold text-slate-500",children:"검색 결과가 없습니다."})}):e.jsxs(e.Fragment,{children:[" ",o===0&&e.jsxs("div",{children:[e.jsx(y,{type:u[1].type,name:u[1].name,data:h,setSelectedCate:n,searchValue:x,searchData:s,setSearchData:l,dataIndex:a,setDataIndex:r,category:1}),e.jsx(y,{type:u[2].type,name:u[2].name,data:p,setSelectedCate:n,searchValue:x,searchData:s,setSearchData:l,dataIndex:a,setDataIndex:r,category:2}),e.jsx(y,{type:u[3].type,name:u[3].name,data:g,setSelectedCate:n,searchValue:x,searchData:s,setSearchData:l,dataIndex:a,setDataIndex:r,category:3}),e.jsx(y,{type:u[4].type,name:u[4].name,data:d,setSelectedCate:n,searchValue:x,searchData:s,setSearchData:l,dataIndex:a,setDataIndex:r,category:4})]}),o===1&&e.jsx("div",{children:e.jsx(y,{type:"TOUR",data:h,searchValue:x,searchData:s,setSearchData:l,dataIndex:a,setDataIndex:r,setSelectedCate:n,category:1})}),o===2&&e.jsx("div",{children:e.jsx(y,{type:"STAY",data:p,searchValue:x,searchData:s,setSearchData:l,dataIndex:a,setDataIndex:r,setSelectedCate:n,category:2})}),o===3&&e.jsx("div",{children:e.jsx(y,{type:"RESTAUR",data:g,searchValue:x,searchData:s,setSearchData:l,dataIndex:a,setDataIndex:r,setSelectedCate:n,category:3})}),o===4&&e.jsx("div",{children:e.jsx(y,{type:"FEST",data:d,searchValue:x,searchData:s,setSearchData:l,dataIndex:a,setDataIndex:r,setSelectedCate:n,category:4})})]})]})},ye=()=>{k(),v("accessToken"),A(),I().state;const[s,l]=c.useState(!1),[o,n]=c.useState(""),[a,r]=c.useState([]),[m,h]=c.useState("");c.useEffect(()=>{},[a]);const p=async()=>{const g={search_word:o,last_index:0};console.log("검색:",g);try{const d=await N.post(`/api/search/all?search_word=${o}&last_index=1`,{...g});console.log("검색 결과 호출",d.data);const i=d.data;r([...i.data])}catch(d){console.log(d)}};return c.useEffect(()=>{console.log("searchValue:",o),p()},[o]),e.jsxs("div",{className:"w-full flex flex-col gap-[30px]",children:[e.jsx($,{searchValue:o,setSearchValue:n,setSearchState:l,inputValue:m,setInputValue:h,searchData:a,setSearchData:r}),s?e.jsx(F,{searchValue:o,searchData:a,setSearchData:r}):e.jsx(U,{setSearchState:l,searchData:a,setSearchData:r,setSearchValue:n,searchValue:o}),e.jsx(P,{})]})};export{ye as default};
