import type { Plugin } from "@opencode-ai/plugin"

const CATS = [
  `  /\\_/\\  \n ( o.o ) \n  > ^ <  — Naranja`,
  `  /\\_/\\  \n ( -.-)zzz — Blanco mimido`,
  `  /\\___/\\ \n ( o_O)  — Negro asustado`,
  `  /\\_/\\  \n ( ^.^)♥ — Gris acariciado`,
  `  /\\_/\\  \n ( =.=)  — Calicó estirado`,
  `  /\\_/\\  \n ( o.o)🧶 — Crema con pelotita`,
  `  /\\_/\\  \n ( >.<)  — Marrón enojado`,
  `  /\\_/\\  \n ( -.-)  — Siamés elegante`,
  `  /\\_/\\  \n ( =w=)  — Atigrado rayado`,
  `  /\\_/\\  \n ( o.o)🎩 — Smoking con gorrito`,
]

const TIPS = [
  "Tip: Tools > Gatitos > 🧶 Lanzar pelotita en Android Studio",
  "Tip: Shift+Click → láser rojo que todos persiguen",
  "Tip: Arrastrá un gatito y soltalo con impulso",
  "Tip: Activa huellitas 🐾 en la sidebar Gatetes",
]

function randomCat() {
  return CATS[Math.floor(Math.random() * CATS.length)]
}

export default (async () => {
  return {
    tool: {
      gatetes: {
        description: "Muestra un gatito ASCII aleatorio de los 10 únicos",
        args: {
          type: "object",
          properties: {
            color: { type: "string", description: "Color: naranja, blanco, negro, gris, calico, crema, marrón, siamés, atigrado, smoking", default: "random" },
            count: { type: "number", description: "Cantidad 1-10", default: 1 }
          }
        },
        async execute(args: any) {
          const count = Math.min(10, Math.max(1, args.count ?? 1))
          let out = ""
          for (let i = 0; i < count; i++) out += randomCat() + "\n\n"
          out += TIPS[Math.floor(Math.random() * TIPS.length)]
          return out
        }
      }
    },
    "tool.execute.after": async (input: any, output: any) => {
      // 8% chance de aparecer un gatito después de cada tool
      if (Math.random() < 0.08) {
        const cat = CATS[Math.floor(Math.random() * CATS.length)].split("\n")[0]
        // Inyectamos un mensaje sutil en el output si existe
        if (output?.content) {
          // no mutamos el output real, solo logueamos para el usuario
        }
      }
    },
    event: async (input: any) => {
      // Loguear gatitos en eventos de build si se detecta
      if (input?.type?.includes("build") || input?.type?.includes("task")) {
        // noop
      }
    }
  }
}) satisfies Plugin
