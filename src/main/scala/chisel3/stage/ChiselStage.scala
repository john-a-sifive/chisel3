// See LICENSE for license details.

package chisel3.stage

import firrtl.AnnotationSeq
import firrtl.options.{Phase, Shell, Stage}
import firrtl.options.Viewer.view
import firrtl.stage.{FirrtlCli, FirrtlStage}

class ChiselStage extends Stage {
  val shell: Shell = new Shell("chisel") with ChiselCli with FirrtlCli

  private val phases: Seq[Phase] =
    Seq( new chisel3.stage.phases.Checks,
         new chisel3.stage.phases.Elaborate,
         new chisel3.stage.phases.AddImplicitOutputFile,
         new chisel3.stage.phases.AddImplicitOutputAnnotationFile,
         new chisel3.stage.phases.Emitter,
         new chisel3.stage.phases.Convert )
      .map(firrtl.options.phases.DeletedWrapper(_))

  def run(annotations: AnnotationSeq): AnnotationSeq = {
    val cOpts = view[ChiselOptions](annotations)

    /* @todo: Should this be wrapped in a try/catch? */
    (phases ++
       (if (cOpts.runFirrtlCompiler) { Some(new FirrtlStage) }
        else                         { Seq.empty         }))
      .foldLeft(annotations)( (a, f) => f.transform(a) )
  }

}