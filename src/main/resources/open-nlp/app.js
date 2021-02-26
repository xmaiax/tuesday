/*
ARTIGO("art", arrayOf("art")),
NUMERAL("num", arrayOf("num")),
PONTUACAO("punc", arrayOf("pont")),
SUBSTANTIVO("n", arrayOf("sm", "sf")),
ADJETIVO("adj", arrayOf("adj")),
ADVERBIO("adv", arrayOf("adv")),
NOME_PROPRIO("prop", arrayOf("prop")),
PREPOSICAO("prp", arrayOf("prp", "prep")),
INTERJEICAO("in", arrayOf("int")),
PRONOME_DETERMINATIVO("pron-det", arrayOf("pron")),
PRONOME_INDEPENDENTE("pron-indp", arrayOf("pron")),
PRONOME_PESSOAL("pron-pers", arrayOf("pron")),
VERBO_FINITO("v-fin", arrayOf("v")),
VERBO_GERUNDIO("v-ger", arrayOf("v")),
VERBO_INFINITIVO("v-inf", arrayOf("v")),
VERBO_PARTICIPIO("v-pcp", arrayOf("v")),
CONJUNCAO_COORDENATIVA("conj-c", arrayOf("conj")),
CONJUNCAO_SUBORDINATIVA("conj-s", arrayOf("conj")),
SINTAGMA_EVIDENCIADOR_REL_COORDENACAO("ec", arrayOf("sint", "ec")),
SINTAGMA_PREPOSICIONAL("pp", arrayOf("sint", "pp")),
SINTAGMA_VERBAL("vp", arrayOf("sint", "vp")),
*/

const fs = require('fs')
const defaultEncoding = 'utf-8'

const unqTags = []
const allTags = []
const tagCounts = {}

fs.readFile(`./raw-dict.csv`, defaultEncoding, (ex, content) => {
  
  if(ex) {
    console.log(ex)
    return
  }
  
  content.split('\n').map((line) => {
    const [word,tag,lemma] = line.split(';')
	
	tag.split(' ').filter((t) => !!t && t.trim().length).forEach((t) => {
      if(unqTags.indexOf(t.trim()) === -1) unqTags.push(t.trim())
	  allTags.push(t.trim())
	})
	
  })
  
  allTags.forEach((t) => {
	tagCounts[t] = {
	  'tag': t,
	  'qtd': (tagCounts[t] && tagCounts[t].qtd || 0) + 1
	}
  })
  
  console.log(Object.entries(tagCounts).map((x) => x[1])
    .filter((x) => x.qtd > 4)
    .sort((x, y) => x.qtd < y.qtd ? 1 : -1))
  
})
